package org.opendaylight.aaa.authz.srv;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.BrokerService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;

import java.util.concurrent.Future;

/**
 * Created by wdec on 28/08/2014.
 */
public class AuthzConsumerContextImpl implements Broker.ConsumerSession {

  private final Broker.ConsumerSession realSession;

  public AuthzConsumerContextImpl(Broker.ConsumerSession realSession, AuthzBrokerImpl authzBroker) {
    this.realSession = realSession;
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
  public void close() {
    realSession.close();
  }
}
