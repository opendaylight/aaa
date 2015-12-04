package org.opendaylight.aaa.cluster;

/**
 * Created by root on 12/4/15.
 */
public interface AAAClusterListener {
    public void receivedObject(Object object,int op);
}
