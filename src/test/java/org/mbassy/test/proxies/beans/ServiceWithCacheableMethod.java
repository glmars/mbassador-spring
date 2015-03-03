package org.mbassy.test.proxies.beans;

import org.springframework.cache.annotation.Cacheable;

/**
 * Interface method is annotated.
 */
public interface ServiceWithCacheableMethod
{
	@Cacheable("testCache")
	Object retrieveObject();
}
