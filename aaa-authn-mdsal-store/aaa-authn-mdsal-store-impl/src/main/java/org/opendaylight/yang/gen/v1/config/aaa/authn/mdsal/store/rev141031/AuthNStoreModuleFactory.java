/*
* Generated file
*
* Generated from: yang module name: aaa-authn-mdsal-store-cfg yang module local name: aaa-authn-mdsal-store
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Thu Mar 19 18:06:18 CET 2015
*
* Do not modify this file unless it is present under src/main directory
*/
package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

public class AuthNStoreModuleFactory extends org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.AbstractAuthNStoreModuleFactory {

  @Override
  public AuthNStoreModule instantiateModule(String instanceName,
                                            DependencyResolver dependencyResolver, BundleContext bundleContext) {
    AuthNStoreModule module = super.instantiateModule(instanceName, dependencyResolver, bundleContext);
    module.setBundleContext(bundleContext);
    return module;
  }

  @Override
  public AuthNStoreModule instantiateModule(String instanceName,
                                            DependencyResolver dependencyResolver, AuthNStoreModule oldModule,
                                            AutoCloseable oldInstance, BundleContext bundleContext) {
    AuthNStoreModule module = super.instantiateModule(instanceName, dependencyResolver, oldModule,
      oldInstance, bundleContext);
    module.setBundleContext(bundleContext);
    return module;
  }
}
