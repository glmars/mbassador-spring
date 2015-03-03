package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service with a cacheable method from the interface.
 */
@Service
public class ServiceWithoutCacheableImplCacheableOnMethod implements ServiceWithoutCacheable
{
	@Cacheable("testCache")
	@Override
	public Object retrieveObject() {
		return "String object";
	}

	@Handler
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithoutCacheableImplCacheableOnMethod.class );
	}
}
