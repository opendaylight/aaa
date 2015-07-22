package org.opendaylight.aaa.idm.persistence;

public interface IJDBCStoreVisitor {
    public String getStoreDataTypeName(String javaType);
    public String getStoreNotNullStatement();
}
