package org.mbassy.test.proxies.beans;

import net.engio.mbassy.listener.Handler;
import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.stereotype.Service;

@Service
public class ServiceWithCacheableMethodImpl implements ServiceWithCacheableMethod
{
	@Override
	public Object retrieveObject() {
		return null;
	}

	@Handler
	public void handle( ListenerTrackingMessage message ) {
		message.markReceived( ServiceWithCacheableMethodImpl.class );
	}
}
