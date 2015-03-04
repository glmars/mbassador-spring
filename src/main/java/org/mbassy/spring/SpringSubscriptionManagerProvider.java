package org.mbassy.spring;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.ISubscriptionManagerProvider;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;

public class SpringSubscriptionManagerProvider implements ISubscriptionManagerProvider
{
	@Override
	public SubscriptionManager createManager(
			MetadataReader reader, SubscriptionFactory factory, BusRuntime runtime ) {
		return new SpringSubscriptionManager( reader, factory, runtime );
	}
}
