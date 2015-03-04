package org.mbassy.spring;

import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyMessageHandler extends MessageHandler
{
	private final MessageHandler targetHandler;
	private final Method proxyMethod;

	public ProxyMessageHandler(
			MessageHandler targetHandler, Method proxyMethod ) {
		super();
		this.targetHandler = targetHandler;
		this.proxyMethod = proxyMethod;
	}

	public ProxyMessageHandler( MessageHandler targetHandler ) {
		super();
		this.targetHandler = targetHandler;
		this.proxyMethod = null;
	}

	@Override
	public boolean acceptsSubtypes() {
		return targetHandler.acceptsSubtypes();
	}

	@Override
	public boolean handlesMessage( Class<?> messageType ) {
		return targetHandler.handlesMessage( messageType );
	}

	@Override
	public Class<? extends HandlerInvocation> getHandlerInvocation() {
		return targetHandler.getHandlerInvocation();
	}

	@Override
	public boolean isEnveloped() {
		return targetHandler.isEnveloped();
	}

	@Override
	public Class[] getHandledMessages() {
		return targetHandler.getHandledMessages();
	}

	@Override
	public String getCondition() {
		return targetHandler.getCondition();
	}

	@Override
	public IMessageFilter[] getFilter() {
		return targetHandler.getFilter();
	}

	@Override
	public Method getMethod() {
		return targetHandler.getMethod();
	}

	@Override
	public int getPriority() {
		return targetHandler.getPriority();
	}

	@Override
	public boolean isFiltered() {
		return targetHandler.isFiltered();
	}

	@Override
	public boolean isAsynchronous() {
		return targetHandler.isAsynchronous();
	}

	@Override
	public boolean isFromListener( Class listener ) {
		return targetHandler.isFromListener( listener );
	}

	@Override
	public boolean useStrongReferences() {
		return targetHandler.useStrongReferences();
	}

	@Override
	public boolean isSynchronized() {
		return targetHandler.isSynchronized();
	}

	@Override
	public void invoke(
			Object listener,
			Object message ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ( proxyMethod == null ) {
			targetHandler.invoke( getProxyTarget( listener ), message );
		}
		else {
			proxyMethod.invoke( listener, message );
		}
	}

	private static Object getProxyTarget( Object instance ) throws InvocationTargetException {
		try {
			if ( AopUtils.isJdkDynamicProxy( instance ) ) {
				return getProxyTarget( ( (Advised) instance ).getTargetSource().getTarget() );
			}
		}
		catch ( Exception e ) {
			throw new InvocationTargetException( e );
		}

		return instance;
	}
}
