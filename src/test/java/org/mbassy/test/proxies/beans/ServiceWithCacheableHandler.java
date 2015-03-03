package org.mbassy.test.proxies.beans;

import org.mbassy.test.messages.ListenerTrackingMessage;
import org.springframework.cache.annotation.Cacheable;

/**
 * Handler method is declared on the interface and is annotated there.
 * TODO: looks like the @Cacheable annotation is not intercepted.
 */
public interface ServiceWithCacheableHandler
{
	Object retrieveObject();

	@Cacheable(value = "testCache", key = "'ServiceWithCacheableHandler'")
	void handle( ListenerTrackingMessage message );
}
