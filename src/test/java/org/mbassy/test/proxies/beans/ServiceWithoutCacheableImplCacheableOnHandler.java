package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service with a cacheable handler method that is not defined on the interface.
 */
@Service
public class ServiceWithoutCacheableImplCacheableOnHandler implements ServiceWithoutCacheable
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Cacheable(value = "testCache", key = "'ServiceWithoutCacheableImplCacheableOnHandler'")
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithoutCacheableImplCacheableOnHandler.class );
	}
}
