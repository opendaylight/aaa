/*
* Generated file
*
* Generated from: yang module name: aaa-authz-service-impl yang module local name: aaa-authz-service
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Thu Jul 24 11:19:40 CEST 2014
*
* Do not modify this file unless it is present under src/main directory
*/
package org.opendaylight.controller.config.yang.config.aaa_authz.srv;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.DynamicMBeanWithInstance;
import org.opendaylight.controller.config.spi.Module;
import org.osgi.framework.BundleContext;

public class AuthzSrvModuleFactory extends org.opendaylight.controller.config.yang.config.aaa_authz.srv.AbstractAuthzSrvModuleFactory {

  @Override
  public org.opendaylight.controller.config.spi.Module createModule(String instanceName, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.osgi.framework.BundleContext bundleContext) {

    final AuthzSrvModule module = (AuthzSrvModule) super.createModule(instanceName, dependencyResolver, bundleContext);

    module.setBundleContext(bundleContext);

    return module;

  }

  @Override
  public Module createModule(final String instanceName, final DependencyResolver dependencyResolver,
                             final DynamicMBeanWithInstance old, final BundleContext bundleContext) throws Exception {
    final AuthzSrvModule module = (AuthzSrvModule) super.createModule(instanceName, dependencyResolver,
        old, bundleContext);

    module.setBundleContext(bundleContext);

    return module;
  }
}
