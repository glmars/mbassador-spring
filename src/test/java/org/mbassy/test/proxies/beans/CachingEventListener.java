package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Class that does not implement any interface but has an annotated handler method.
 */
@Component
public class CachingEventListener
{
	@Handler
	@Cacheable(value = "testCache", key = "'CachingEventListener'")
	public void handle( ListenerTrackingMessage listenerTrackingMessage ) {
		listenerTrackingMessage.markReceived( CachingEventListener.class );
	}
}
