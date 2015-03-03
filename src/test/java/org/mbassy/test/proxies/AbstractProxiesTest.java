package org.mbassy.test.proxies;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.mbassy.test.proxies.beans.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AbstractProxiesTest.Config.class)
public abstract class AbstractProxiesTest
{
	@Autowired
	@Qualifier("serviceWithCacheableHandlerImpl")
	protected ServiceWithCacheableHandler serviceWithCacheableHandlerImpl;

	@Autowired
	@Qualifier("serviceWithCacheableHandlerImplCacheableOnHandler")
	protected ServiceWithCacheableHandler serviceWithCacheableHandlerImplCacheableOnHandler;

	@Autowired
	@Qualifier("serviceWithCacheableMethodImpl")
	protected ServiceWithCacheableMethod serviceWithCacheableMethodImpl;

	@Autowired
	@Qualifier("serviceWithCacheableMethodImplCacheableOnHandler")
	protected ServiceWithCacheableMethod serviceWithCacheableMethodImplCacheableOnHandler;

	@Autowired
	@Qualifier("serviceWithHandlerImplCacheableOnHandler")
	protected ServiceWithHandler serviceWithHandlerImplCacheableOnHandler;

	@Autowired
	@Qualifier("serviceWithHandlerImplCacheableOnMethod")
	protected ServiceWithHandler serviceWithHandlerImplCacheableOnMethod;

	@Autowired
	@Qualifier("serviceWithoutCacheableImplCacheableOnHandler")
	protected ServiceWithoutCacheable serviceWithoutCacheableImplCacheableOnHandler;

	@Autowired
	@Qualifier("serviceWithoutCacheableImplCacheableOnMethod")
	protected ServiceWithoutCacheable serviceWithoutCacheableImplCacheableOnMethod;

	@Autowired
	protected CachingEventListener cachingEventListener;

	@Autowired
	protected SimpleEventListener simpleEventListener;

	@Autowired
	protected Cache testCache;

	protected ListenerTrackingMessage message;

	@Before
	public void prepareForTests() {
		reset( testCache );
		when( testCache.getName() ).thenReturn( "testCache" );
		when( testCache.get( anyObject() ) ).thenReturn( new SimpleValueWrapper( Boolean.FALSE ) );
		when( testCache.get( anyObject(), any( Class.class ) ) ).thenReturn( new SimpleValueWrapper( Boolean.FALSE ) );

		message = new ListenerTrackingMessage();
	}

	protected void assertJdkProxy( Object bean ) {
		assertTrue( AopUtils.isAopProxy( bean ) );
		assertTrue( AopUtils.isJdkDynamicProxy( bean ) );
		assertFalse( AopUtils.isCglibProxy( bean ) );
	}

	protected void assertCglibProxy( Object bean ) {
		assertTrue( AopUtils.isAopProxy( bean ) );
		assertFalse( AopUtils.isJdkDynamicProxy( bean ) );
		assertTrue( AopUtils.isCglibProxy( bean ) );
	}

	@Configuration
	@ComponentScan("org.mbassy.test.proxies.beans")
	static class Config
	{
		@Bean
		public Cache testCache() {
			Cache testCache = mock( Cache.class );
			when( testCache.getName() ).thenReturn( "testCache" );

			return testCache;
		}

		@Bean
		public CacheManager simpleCacheManager() {
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches( Arrays.asList( testCache() ) );

			return cacheManager;
		}
	}
}
