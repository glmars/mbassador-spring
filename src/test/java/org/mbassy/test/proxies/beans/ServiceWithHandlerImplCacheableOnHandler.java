package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Handler method is also on the interface and has Cacheable annotation.
 */
@Service
public class ServiceWithHandlerImplCacheableOnHandler implements ServiceWithHandler
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Cacheable(value = "testCache", key = "'ServiceWithHandlerImplCacheableOnHandler'")
	@Override
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithHandlerImplCacheableOnHandler.class );
	}
}
