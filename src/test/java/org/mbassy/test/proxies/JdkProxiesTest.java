package org.mbassy.test.proxies;

import org.junit.Test;
import org.mbassy.spring.TransactionAwareMessageBus;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.mbassy.test.proxies.beans.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests that AOP proxies are registered correctly on the message bus.
 */
@ContextConfiguration(classes = JdkProxiesTest.UseJdkConfig.class)
public class JdkProxiesTest extends AbstractProxiesTest
{
	@Test
	public void verifyCreatedProxies() {
		assertJdkProxy( serviceWithCacheableHandlerImpl );
		assertJdkProxy( serviceWithCacheableHandlerImplCacheableOnHandler );

		assertJdkProxy( serviceWithCacheableMethodImpl );
		assertJdkProxy( serviceWithCacheableMethodImplCacheableOnHandler );

		assertJdkProxy( serviceWithHandlerImplCacheableOnMethod );
		assertJdkProxy( serviceWithHandlerImplCacheableOnHandler );

		assertJdkProxy( serviceWithoutCacheableImplCacheableOnMethod );
		assertJdkProxy( serviceWithoutCacheableImplCacheableOnHandler );

		assertCglibProxy( simpleEventListener );
		assertCglibProxy( cachingEventListener );
	}

	@Test
	public void defaultBusSupportsCglibEnhancedClasses() {
		TransactionAwareMessageBus<ListenerTrackingMessage> bus = new TransactionAwareMessageBus<ListenerTrackingMessage>();

		// Subscribe all event listeners
		bus.subscribe( simpleEventListener );
		bus.subscribe( cachingEventListener );

		bus.publish( message );

		assertTrue( message.isReceiver( SimpleEventListener.class ) );
		assertFalse( message.isReceiver( CachingEventListener.class ) );

		// Verify cache intercepts
		verify( testCache, atLeastOnce() ).get( "CachingEventListener" );
	}

	@Test
	public void defaultBusDoesNotSupportJdkProxies() {
		TransactionAwareMessageBus<ListenerTrackingMessage> bus = new TransactionAwareMessageBus<ListenerTrackingMessage>();

		// Subscribe all event listeners
		bus.subscribe( serviceWithCacheableHandlerImpl );
		bus.subscribe( serviceWithCacheableHandlerImplCacheableOnHandler );
		bus.subscribe( serviceWithCacheableMethodImpl );
		bus.subscribe( serviceWithCacheableMethodImplCacheableOnHandler );
		bus.subscribe( serviceWithHandlerImplCacheableOnHandler );
		bus.subscribe( serviceWithHandlerImplCacheableOnMethod );
		bus.subscribe( serviceWithoutCacheableImplCacheableOnHandler );
		bus.subscribe( serviceWithoutCacheableImplCacheableOnMethod );

		bus.publish( message );

		assertFalse( message.isReceiver( ServiceWithCacheableHandlerImpl.class ) );
		assertFalse( message.isReceiver( ServiceWithCacheableHandlerImplCacheableOnHandler.class ) );
		assertFalse( message.isReceiver( ServiceWithCacheableMethodImpl.class ) );
		assertFalse( message.isReceiver( ServiceWithCacheableMethodImplCacheableOnHandler.class ) );
		assertFalse( message.isReceiver( ServiceWithHandlerImplCacheableOnHandler.class ) );
		assertFalse( message.isReceiver( ServiceWithHandlerImplCacheableOnMethod.class ) );
		assertFalse( message.isReceiver( ServiceWithoutCacheableImplCacheableOnHandler.class ) );
		assertFalse( message.isReceiver( ServiceWithoutCacheableImplCacheableOnMethod.class ) );

		// Verify cache intercepts
		verify( testCache, never() ).get( "ServiceWithCacheableHandler" );
		verify( testCache, never() ).get( "ServiceWithCacheableHandlerImplCacheableOnHandler" );
		verify( testCache, never() ).get( "ServiceWithCacheableMethodImplCacheableOnHandler" );
		verify( testCache, never() ).get( "ServiceWithHandlerImplCacheableOnHandler" );
		verify( testCache, never() ).get( "ServiceWithoutCacheableImplCacheableOnHandler" );
	}

	@Configuration
	@EnableCaching(proxyTargetClass = false, mode = AdviceMode.PROXY)
	static class UseJdkConfig
	{
	}
}
