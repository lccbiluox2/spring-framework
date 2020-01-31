/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.remoting.httpinvoker;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link FactoryBean} for HTTP invoker proxies. Exposes the proxied service
 * for use as a bean reference, using the specified service interface.
 *
 * <p>The service URL must be an HTTP URL exposing an HTTP invoker service.
 * Optionally, a codebase URL can be specified for on-demand dynamic code download
 * from a remote location. For details, see HttpInvokerClientInterceptor docs.
 *
 * <p>Serializes remote invocation objects and deserializes remote invocation
 * result objects. Uses Java serialization just like RMI, but provides the
 * same ease of setup as Caucho's HTTP-based Hessian protocol.
 *
 * <p><b>HTTP invoker is the recommended protocol for Java-to-Java remoting.</b>
 * It is more powerful and more extensible than Hessian, at the expense of
 * being tied to Java. Nevertheless, it is as easy to set up as Hessian,
 * which is its main advantage compared to RMI.
 *
 * <p><b>WARNING: Be aware of vulnerabilities due to unsafe Java deserialization:
 * Manipulated input streams could lead to unwanted code execution on the server
 * during the deserialization step. As a consequence, do not expose HTTP invoker
 * endpoints to untrusted clients but rather just between your own services.</b>
 * In general, we strongly recommend any other message format (e.g. JSON) instead.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see #setCodebaseUrl
 * @see HttpInvokerClientInterceptor
 * @see HttpInvokerServiceExporter
 * @see org.springframework.remoting.rmi.RmiProxyFactoryBean
 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean
 */
public class HttpInvokerProxyFactoryBean extends HttpInvokerClientInterceptor implements FactoryBean<Object> {

	// 这是远端对象的代理
	@Nullable
	private Object serviceProxy;


	/**
	 * 在注入完成后设置远端对象代理
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		// 需要配置远端调用的接口
		Class<?> ifc = getServiceInterface();
		Assert.notNull(ifc, "Property 'serviceInterface' is required");
		/**
		 * 这里使用 ProxyFacetory来生成远端代理端对象，注意这个this，因为HttpInvokerProxyFactoryBean的基类是HttpInvokerClientInterceptor，所以代理
		 * 类的拦截器被设置为HttpInvokerCientInterceptor
		 */
		this.serviceProxy = new ProxyFactory(ifc, this).getProxy(getBeanClassLoader());
	}


	/**
	 * FactoryBean生成对象的入口，返回的是serviceProxy对象，这是一个代理对象
	 * @return
	 */
	@Override
	@Nullable
	public Object getObject() {
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
