package org.opendaylight.aaa.authz.srv;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationResponseType;

/**
 * Created by wdec on 28/08/2014.
 */

public class AuthzReadOnlyTransaction implements DOMDataReadOnlyTransaction {

  private final DOMDataReadOnlyTransaction ro;
  public AuthzReadOnlyTransaction(DOMDataReadOnlyTransaction ro) {
    this.ro = ro;
  }

  @Override
  public void close() {
    ro.close();
  }

  @Override
  public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.

    AuthorizationResponseType authorizationResponseType = AuthzServiceImpl.reqAuthorization(ActionType.Read,logicalDatastoreType,yangInstanceIdentifier);

      if(authorizationResponseType.equals(AuthorizationResponseType.Authorized)){
          return ro.read(logicalDatastoreType, yangInstanceIdentifier);
      }else{
          return null;
      }

  }

  @Override
  public CheckedFuture<Boolean, ReadFailedException> exists(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    return ro.exists(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public Object getIdentifier() {
    return ro.getIdentifier();
  }
}
