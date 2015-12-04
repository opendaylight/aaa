package org.opendaylight.aaa.cluster;

/**
 * Created by root on 12/4/15.
 */
public interface AAAClusterService {
    public void writeObject(Object object);
    public void updateObject(Object object);
    public void deleteObject(Object object);
}
