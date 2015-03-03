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

@ContextConfiguration(classes = CglibProxiesTest.UseCglibConfig.class)
public class CglibProxiesTest extends AbstractProxiesTest
{
	@Test
	public void verifyCreatedProxies() {
		assertCglibProxy( serviceWithCacheableHandlerImpl );
		assertCglibProxy( serviceWithCacheableHandlerImplCacheableOnHandler );

		assertCglibProxy( serviceWithCacheableMethodImpl );
		assertCglibProxy( serviceWithCacheableMethodImplCacheableOnHandler );

		assertCglibProxy( serviceWithHandlerImplCacheableOnMethod );
		assertCglibProxy( serviceWithHandlerImplCacheableOnHandler );

		assertCglibProxy( serviceWithoutCacheableImplCacheableOnMethod );
		assertCglibProxy( serviceWithoutCacheableImplCacheableOnHandler );

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
	public void defaultBusSupportsCglibProxies() {
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

		assertTrue( message.isReceiver( ServiceWithCacheableHandlerImpl.class ) );
		assertFalse( message.isReceiver( ServiceWithCacheableHandlerImplCacheableOnHandler.class ) );
		assertTrue( message.isReceiver( ServiceWithCacheableMethodImpl.class ) );
		assertFalse( message.isReceiver( ServiceWithCacheableMethodImplCacheableOnHandler.class ) );
		assertFalse( message.isReceiver( ServiceWithHandlerImplCacheableOnHandler.class ) );
		assertTrue( message.isReceiver( ServiceWithHandlerImplCacheableOnMethod.class ) );
		assertFalse( message.isReceiver( ServiceWithoutCacheableImplCacheableOnHandler.class ) );
		assertTrue( message.isReceiver( ServiceWithoutCacheableImplCacheableOnMethod.class ) );

		// Verify cache intercepts
		verify( testCache, never() ).get( "ServiceWithCacheableHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithCacheableHandlerImplCacheableOnHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithCacheableMethodImplCacheableOnHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithHandlerImplCacheableOnHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithoutCacheableImplCacheableOnHandler" );
	}

	@Configuration
	@EnableCaching(proxyTargetClass = true, mode = AdviceMode.PROXY)
	static class UseCglibConfig
	{
	}
}
