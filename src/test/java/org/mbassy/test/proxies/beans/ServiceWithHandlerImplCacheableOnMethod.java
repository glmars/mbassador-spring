package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Handler method is also on the interface.  Cacheable is on the other method.
 */
@Service
public class ServiceWithHandlerImplCacheableOnMethod implements ServiceWithHandler
{
	@Cacheable("testCache")
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Override
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithHandlerImplCacheableOnMethod.class );
	}
}
