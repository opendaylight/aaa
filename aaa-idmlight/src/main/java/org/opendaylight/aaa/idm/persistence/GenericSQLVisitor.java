package org.opendaylight.aaa.idm.persistence;

public class GenericSQLVisitor implements IJDBCStoreVisitor{

    @Override
    public String getStoreDataTypeName(String javaType) {
        if(javaType.toLowerCase().equals("int") || javaType.toLowerCase().equals("integer"))
            return "INTEGER";
        else
        if(javaType.toLowerCase().equals("string")){
            return "VARCHAR(";
        }else
        if(javaType.toLowerCase().equals("boolean")){
            return "INTEGER";
        }
        return "Unknown Type";
    }

    @Override
    public String getStoreNotNullStatement() {
        return "Not NULL";
    }    
}
