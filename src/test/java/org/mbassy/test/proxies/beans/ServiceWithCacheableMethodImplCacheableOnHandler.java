package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ServiceWithCacheableMethodImplCacheableOnHandler implements ServiceWithCacheableMethod
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Cacheable(value = "testCache", key = "'ServiceWithCacheableMethodImplCacheableOnHandler'")
	public void handler( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithCacheableMethodImplCacheableOnHandler.class );
	}
}
