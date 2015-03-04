package org.mbassy.spring;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SpringSubscriptionManager extends SubscriptionManager
{

	// the metadata reader that is used to inspect objects passed to the subscribe method
	private final MetadataReader metadataReader;

	// all subscriptions per message type
	// this is the primary list for dispatching a specific message
	// write access is synchronized and happens only when a listener of a specific class is registered the first time
	private final Map<Class, Collection<Subscription>> subscriptionsPerMessage =
			new HashMap<Class, Collection<Subscription>>( 50 );

	// all subscriptions per messageHandler type
	// this map provides fast access for subscribing and unsubscribing
	// write access is synchronized and happens very infrequently
	// once a collection of subscriptions is stored it does not change
	private final Map<Class, Collection<Subscription>> subscriptionsPerListener =
			new HashMap<Class, Collection<Subscription>>( 50 );

	// remember already processed classes that do not contain any message handlers
	private final StrongConcurrentSet<Class> nonListeners = new StrongConcurrentSet<Class>();

	// this factory is used to create specialized subscriptions based on the given message handler configuration
	// it can be customized by implementing the getSubscriptionFactory() method
	private final SubscriptionFactory subscriptionFactory;

	// synchronize read/write acces to the subscription maps
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private final BusRuntime runtime;

	public SpringSubscriptionManager(
			MetadataReader metadataReader, SubscriptionFactory subscriptionFactory, BusRuntime runtime ) {
		super( metadataReader, subscriptionFactory, runtime );

		this.subscriptionFactory = subscriptionFactory;
		this.runtime = runtime;
		this.metadataReader = metadataReader;
	}

	public boolean unsubscribe( Object listener ) {
		if ( listener == null ) {
			return false;
		}
		Collection<Subscription> subscriptions = getSubscriptionsByListener(determineListenerClass( listener ) );
		if ( subscriptions == null ) {
			return false;
		}
		boolean isRemoved = true;
		for ( Subscription subscription : subscriptions ) {
			isRemoved &= subscription.unsubscribe( listener );
		}
		return isRemoved;
	}

	private Collection<Subscription> getSubscriptionsByListener( Class listenerClass ) {
		Collection<Subscription> subscriptions;
		try {
			readWriteLock.readLock().lock();
			subscriptions = subscriptionsPerListener.get( listenerClass );
		}
		finally {
			readWriteLock.readLock().unlock();
		}
		return subscriptions;
	}

	private Class determineListenerClass( Object listener ){
		return AopUtils.getTargetClass( listener );
	}

	public void subscribe( Object listener ) {
		try {
			Class listenerClass = determineListenerClass( listener );

			if ( isKnownNonListener( listenerClass ) ) {
				return; // early reject of known classes that do not define message handlers
			}
			Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener( listenerClass );
			// a listener is either subscribed for the first time
			if ( subscriptionsByListener == null ) {
				List<MessageHandler> messageHandlers =
						metadataReader.getMessageListener( listenerClass ).getHandlers();

				buildProxyHandlers( listener, listenerClass, messageHandlers );

				if ( messageHandlers.isEmpty() ) {  // remember the class as non listening class if no handlers are found
					nonListeners.add( listenerClass );
					return;
				}
				subscriptionsByListener = new ArrayList<Subscription>(
						messageHandlers.size() ); // it's safe to use non-concurrent collection here (read only)
				// create subscriptions for all detected message handlers
				for ( MessageHandler messageHandler : messageHandlers ) {
					// create the subscription
					subscriptionsByListener.add( subscriptionFactory.createSubscription( runtime, messageHandler ) );
				}
				// this will acquire a write lock and handle the case when another thread already subscribed
				// this particular listener in the mean-time
				subscribe( listener, subscriptionsByListener );
			} // or the subscriptions already exist and must only be updated
			else {
				for ( Subscription sub : subscriptionsByListener ) {
					sub.subscribe( listener );
				}
			}

		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	private void buildProxyHandlers( Object listener, Class<?> listenerClass, List<MessageHandler> messageHandlers ) {
		if ( AopUtils.isJdkDynamicProxy( listener ) ) {
			Map<Method, Integer> methodHandlerPosition = new HashMap<Method, Integer>();
			for ( int i = 0; i < messageHandlers.size(); i++ ) {
				MessageHandler handler = messageHandlers.get( i );

				methodHandlerPosition.put( handler.getMethod(), i );
			}

			Class<?> proxyClass = listener.getClass();

			for( Method proxyMethod : org.springframework.util.ReflectionUtils.getAllDeclaredMethods( proxyClass ) ) {
				Method targetMethod = AopUtils.getMostSpecificMethod( proxyMethod, listenerClass );

				Integer handlerIndex = methodHandlerPosition.get( targetMethod );

				if ( handlerIndex != null ) {
					MessageHandler proxyHandler = new ProxyMessageHandler( messageHandlers.get( handlerIndex ), proxyMethod );

					messageHandlers.set( handlerIndex, proxyHandler );
				}
			}

			for ( int i = 0; i<  messageHandlers.size(); i++ ) {
				MessageHandler handler = messageHandlers.get(i);

				if ( !(handler instanceof ProxyMessageHandler ) ) {
					messageHandlers.set( i, new ProxyMessageHandler( handler ));
				}
			}

		}
	}

	private void subscribe( Object listener, Collection<Subscription> subscriptions ) {
		Class listenerClass = determineListenerClass( listener );

		try {
			readWriteLock.writeLock().lock();
			// basically this is a deferred double check
			// it's an ugly pattern but necessary because atomic upgrade from read to write lock
			// is not possible
			// the alternative of using a write lock from the beginning would decrease performance dramatically
			// because of the huge number of reads compared to writes
			Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener( listenerClass );

			if ( subscriptionsByListener == null ) {
				for ( Subscription subscription : subscriptions ) {
					subscription.subscribe( listener );
					for ( Class<?> messageType : subscription.getHandledMessageTypes() ) {
						addMessageTypeSubscription( messageType, subscription );
					}
				}
				subscriptionsPerListener.put( determineListenerClass( listener ), subscriptions );
			}
			// the rare case when multiple threads concurrently subscribed the same class for the first time
			// one will be first, all others will have to subscribe to the existing instead the generated subscriptions
			else {
				for ( Subscription existingSubscription : subscriptionsByListener ) {
					existingSubscription.subscribe( listener );
				}
			}
		}
		finally {
			readWriteLock.writeLock().unlock();
		}

	}

	private boolean isKnownNonListener( Class listenerClass ) {
		return nonListeners.contains( listenerClass );
	}

	// obtain the set of subscriptions for the given message type
	// Note: never returns null!
	public Collection<Subscription> getSubscriptionsByMessageType( Class messageType ) {
		Set<Subscription> subscriptions = new TreeSet<Subscription>( Subscription.SubscriptionByPriorityDesc );
		try {
			readWriteLock.readLock().lock();

			if ( subscriptionsPerMessage.get( messageType ) != null ) {
				subscriptions.addAll( subscriptionsPerMessage.get( messageType ) );
			}
			for ( Class eventSuperType : ReflectionUtils.getSuperTypes( messageType ) ) {
				Collection<Subscription> subs = subscriptionsPerMessage.get( eventSuperType );
				if ( subs != null ) {
					for ( Subscription sub : subs ) {
						if ( sub.handlesMessageType( messageType ) ) {
							subscriptions.add( sub );
						}
					}
				}
			}
		}
		finally {
			readWriteLock.readLock().unlock();
		}
		return subscriptions;
	}

	// associate a subscription with a message type
	// NOTE: Not thread-safe! must be synchronized in outer scope
	private void addMessageTypeSubscription( Class messageType, Subscription subscription ) {
		Collection<Subscription> subscriptions = subscriptionsPerMessage.get( messageType );
		if ( subscriptions == null ) {
			subscriptions = new LinkedList<Subscription>();
			subscriptionsPerMessage.put( messageType, subscriptions );
		}
		subscriptions.add( subscription );
	}
}
