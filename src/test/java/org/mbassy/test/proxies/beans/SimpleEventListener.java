package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Class that defines a handler method, and has another method that is annotated.
 */
@Component
public class SimpleEventListener
{
	@Cacheable("testCache")
	public Object retrieveObject() {
		return null;
	}

	@Handler
	public void handle( ListenerTrackingMessage listenerTrackingMessage ) {
		listenerTrackingMessage.markReceived( SimpleEventListener.class );
	}
}
