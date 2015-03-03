package org.mbassy.test.proxies.beans;

import org.mbassy.test.messages.ListenerTrackingMessage;

/**
 * Handler method is also defined on the interface.
 */
public interface ServiceWithHandler
{
	Object retrieveObject();

	void handle( ListenerTrackingMessage message );
}
