package org.opendaylight.aaa.api;

public class IDMStoreUtil {
    public static String createDomainid(String domainName){
        return domainName;
    }

    public static String createUserid(String username,String domainid){
        return username+"@"+domainid;
    }

    public static String createRoleid(String rolename,String domainid){
        return rolename+"@"+domainid;
    }

    public static String createGrantid(String userid,String domainid,String roleid){
        return  userid+"@"+roleid+"@"+domainid;
    }
}
