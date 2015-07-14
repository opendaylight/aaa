package org.opendaylight.aaa.idm.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

import org.opendaylight.aaa.api.PasswordCredentials;

import com.google.common.base.Preconditions;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

public class LDAPIntegration {

    public static boolean authenticateSSL(PasswordCredentials creds,String domain) throws Exception {
        Preconditions.checkNotNull(creds);
        Preconditions.checkNotNull(domain);
        LDAPConfiguration configuration = LDAPConfiguration.getInstance();
        SSLUtil util = new SSLUtil(null, new TrustAllTrustManager());
        SocketFactory sf;
        LDAPConnection conn = null;
        try {
            sf = util.createSSLSocketFactory();
            conn = new LDAPConnection(sf, configuration.getHost(), configuration.getSSLPort());
        } catch (Exception err) {
            throw new Exception(
                    "LDAP unavailable, Failed to Authenticate user "
                            + creds.username() + " via LDAP.", err.getCause());
        }

        BindResult result = null;
        StringBuffer userDN = new StringBuffer("cn=");
        userDN.append(creds.username()).append(",").append(configuration.getDN());
        BindRequest request = new SimpleBindRequest(userDN.toString(), creds.password());
        request.setResponseTimeoutMillis(configuration.getLDAPTimeout());

        try {
            result = conn.bind(request);
        } catch (Exception err) {
            throw new Exception("Failed to Authenticate user "
                    + creds.username() + " via LDAP.", err.getCause());
        } finally {
            if (conn != null)
                conn.close();
        }

        if (result!=null && ResultCode.SUCCESS.equals(result.getResultCode())) {
            return true;
        } else {
            throw new Exception("Failed to Authenticate user "
                    + creds.username() + " via LDAP.");
        }
    }

    // Future use method demonstrating how to search for roles in a SSL ldap
    // connection
    public static final List<String> sslFindRolesForUser(LDAPConnection conn,String dcdc, String userName) throws LDAPSearchException {
        Filter filter = Filter.createEqualityFilter("cn", userName);
        SearchRequest searchRequest = new SearchRequest(dcdc, SearchScope.SUB,filter, "cn", "mail");
        SearchResult sr = conn.search(searchRequest);
        List<String> result = new ArrayList<String>();
        for (Entry e : sr.getSearchEntries()) {
            result.add(e.getDN());
        }
        /*
         * This is also an example to test when coming to implement this method to support groups extraction
         * Entry groupEntry = conn.getEntry(user); for(Attribute
         * a:groupEntry.getAttributes()){ System.out.print(a.getBaseName());
         * System.out.print(":"); System.out.println(a.getValue()); }
         */
        return result;
    }
    /* Not used, just an example how to do LDAP without SSL
    public static Claim authenticatePlain(PasswordCredentials creds,String domain) throws Exception {
        Hashtable<String, Object> ht = new Hashtable<String, Object>(11);
        LDAPConfiguration configuration = LDAPConfiguration.getInstance();
        ht.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        ht.put(Context.PROVIDER_URL, "ldap://"
                + configuration.getHost() + ":"
                + configuration.getNOSSLPort() + "/o="
                + configuration.getObjectGroup());
        ht.put(Context.SECURITY_AUTHENTICATION, "simple");
        ht.put(Context.SECURITY_PRINCIPAL, "cn=" + creds.username() + "," + configuration.getDN());
        ht.put(Context.SECURITY_CREDENTIALS, creds.password());
        DirContext dirContext = null;
        try {
            dirContext = new InitialDirContext(ht);
        } catch (NamingException ne) {
            throw new Exception("Failed to Authenticate user "
                    + creds.username() + " via LDAP.");
        } finally {
            dirContext.close();
        }

        try {
            User localUser = MFactory.newUser();
            localUser.setName(creds.username());
            localUser = (User) localUser.get();
            // Local User does not exist, create it with a default user role
            if (localUser == null) {
                JSUser jsuser = new JSUser();
                jsuser.setName(creds.username());
                jsuser.setRoleid(2);
                jsuser.write();
                localUser = (User) jsuser.get();
            }

            Grant g = MFactory.newGrant();
            g.setUserid(localUser.getUserid());
            g = (Grant) g.get();

            ClaimBuilder claim = new ClaimBuilder();
            claim.setUserId(localUser.getUserid().toString());
            claim.setUser(creds.username());
            claim.setDomain(domain);
            claim.addRole(g.getRoleid().toString());
            return claim.build();
        } catch (Exception err) {
            throw new Exception(
                    "LDAP unavailable, Failed to Authenticate user "
                            + creds.username() + " via LDAP.", err.getCause());
        }
    }

    public static List<String> nosslFindRolesForUser(DirContext dirContext,String dcdc, String username) {
        try {
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchBase = "DC=cisco,DC=com";
            String filter = "(objectClass=Employees)";
            NamingEnumeration result = dirContext
                    .search(searchBase, filter, sc);
            List<String> r = new ArrayList<String>();
            while (result.hasMoreElements()) {
                r.add(result.next().toString());
            }
            return r;
        } catch (NamingException err) {
            err.printStackTrace();
        }
        return null;
    }*/
}
