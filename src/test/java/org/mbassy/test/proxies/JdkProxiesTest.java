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
	public void defaultBusSupportsJdkProxies() {
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

		assertEquals(
			"no - ServiceWithCacheableHandlerImpl\r" +
			"no - ServiceWithCacheableHandlerImplCacheableOnHandler\r" +
			"yes - ServiceWithCacheableMethodImpl\r" +
			"yes - ServiceWithCacheableMethodImplCacheableOnHandler\r" +
			"no - ServiceWithHandlerImplCacheableOnHandler\r" +
			"yes - ServiceWithHandlerImplCacheableOnMethod\r" +
			"yes - ServiceWithoutCacheableImplCacheableOnHandler\r" +
			"yes - ServiceWithoutCacheableImplCacheableOnMethod\r",
			receiversStatus(
				ServiceWithCacheableHandlerImpl.class,
				ServiceWithCacheableHandlerImplCacheableOnHandler.class,
				ServiceWithCacheableMethodImpl.class,
				// Next one is not cached because the Cacheable is not on the interface
				ServiceWithCacheableMethodImplCacheableOnHandler.class,
				ServiceWithHandlerImplCacheableOnHandler.class,
				ServiceWithHandlerImplCacheableOnMethod.class,
				ServiceWithoutCacheableImplCacheableOnHandler.class,
				ServiceWithoutCacheableImplCacheableOnMethod.class));
		
		// Verify cache intercepts
		verify( testCache, atLeastOnce() ).get( "ServiceWithCacheableHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithCacheableHandlerImplCacheableOnHandler" );
		verify( testCache, never() ).get( "ServiceWithCacheableMethodImplCacheableOnHandler" );
		verify( testCache, atLeastOnce() ).get( "ServiceWithHandlerImplCacheableOnHandler" );
		verify( testCache, never() ).get( "ServiceWithoutCacheableImplCacheableOnHandler" );
	}

	@Configuration
	@EnableCaching(proxyTargetClass = false, mode = AdviceMode.PROXY)
	static class UseJdkConfig
	{
	}
}
