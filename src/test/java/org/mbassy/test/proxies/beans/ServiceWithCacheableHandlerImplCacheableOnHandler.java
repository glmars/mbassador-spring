package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service with a cacheable handler method that is also defined as a cacheable method on the interface.
 */
@Service
public class ServiceWithCacheableHandlerImplCacheableOnHandler implements ServiceWithCacheableHandler
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Cacheable(value = "testCache", key = "'ServiceWithCacheableHandlerImplCacheableOnHandler'")
	@Override
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithCacheableHandlerImplCacheableOnHandler.class );
	}
}
