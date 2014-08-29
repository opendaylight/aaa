package org.opendaylight.aaa.authz.srv;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.BrokerService;
import org.opendaylight.controller.sal.core.api.RpcImplementation;
import org.opendaylight.controller.sal.core.api.RpcRegistrationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by wdec on 28/08/2014.
 */
public class AuthzProviderContextImpl extends AuthzConsumerContextImpl implements Broker.ProviderSession {

  private final Broker.ProviderSession realSession;

  public AuthzProviderContextImpl(Broker.ProviderSession providerSession, AuthzBrokerImpl authzBroker) {
    super(null, authzBroker);
    this.realSession = providerSession;
  }
  @Override
  public Broker.RpcRegistration addRpcImplementation(QName qName, RpcImplementation rpcImplementation) throws IllegalArgumentException {
    return realSession.addRpcImplementation(qName, rpcImplementation);
  }

  @Override
  public Broker.RoutedRpcRegistration addRoutedRpcImplementation(QName qName, RpcImplementation rpcImplementation) {
    return realSession.addRoutedRpcImplementation(qName, rpcImplementation);
  }

  @Override
  public Broker.RoutedRpcRegistration addMountedRpcImplementation(QName qName, RpcImplementation rpcImplementation) {
    return realSession.addMountedRpcImplementation(qName, rpcImplementation);
  }

  @Override
  public void close() {
    realSession.close();

  }

  @Override
  public Future<RpcResult<CompositeNode>> rpc(QName qName, CompositeNode compositeNode) {
    return realSession.rpc(qName, compositeNode);
  }

  @Override
  public boolean isClosed() {
    return realSession.isClosed();
  }

  @Override
  public <T extends BrokerService> T getService(Class<T> tClass) {
    T t;
    //Check for class and return Authz broker only for DOMBroker
    if (tClass == DOMDataBroker.class) {
      t = (T) AuthzDomDataBroker.getInstance();
    }
   else {
      t = realSession.getService(tClass);
    }
   // AuthzDomDataBroker.getInstance().setDomDataBroker((DOMDataBroker)t);
    return t;
  }

  @Override
  public Set<QName> getSupportedRpcs() {
    return realSession.getSupportedRpcs();
  }

  @Override
  public ListenerRegistration<RpcRegistrationListener> addRpcRegistrationListener(RpcRegistrationListener rpcRegistrationListener) {
    return realSession.addRpcRegistrationListener(rpcRegistrationListener);
  }
}
