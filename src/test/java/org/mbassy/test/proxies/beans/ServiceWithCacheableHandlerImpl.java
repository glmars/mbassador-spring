package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.stereotype.Service;

/**
 * Service where the handler method has been annotated with Cacheable on the interface.
 */
@Service
public class ServiceWithCacheableHandlerImpl implements ServiceWithCacheableHandler
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	@Override
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithCacheableHandlerImpl.class );
	}
}
