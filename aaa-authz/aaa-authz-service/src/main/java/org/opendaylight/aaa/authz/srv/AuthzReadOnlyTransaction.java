package org.opendaylight.aaa.authz.srv;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

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
    return ro.read(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public CheckedFuture<Boolean, ReadFailedException> exists(LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {
    //TODO: Do AuthZ check here.
    return ro.exists(logicalDatastoreType, yangInstanceIdentifier);
  }

  @Override
  public Object getIdentifier() {
    //TODO: Do AuthZ check here.
    return ro.getIdentifier();
  }
}
