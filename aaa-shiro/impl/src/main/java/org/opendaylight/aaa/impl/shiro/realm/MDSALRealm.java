package org.opendaylight.aaa.shiro.realm;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.CheckedFuture;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.basic.HttpBasicAuth;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Authentication;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.User;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Users;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


/**
 * Created by ryan on 1/7/17.
 */
public class MDSALRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(MDSALRealm.class);

    private final DataBroker dataBroker;

    public MDSALRealm() {
        this.dataBroker = AAAShiroProvider.getInstance().getDataBroker();
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        LOG.error("doGetAuthorizationInfo " + ((ODLPrincipal)principalCollection.getPrimaryPrincipal()).getRoles());
        return new SimpleAuthorizationInfo(((ODLPrincipal)principalCollection.getPrimaryPrincipal()).getRoles());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username = "";
        String password = "";
        String domain = HttpBasicAuth.DEFAULT_DOMAIN;

        try {
            final String qualifiedUser = extractUsername(authenticationToken);
            if (qualifiedUser.contains("@")) {
                final String [] qualifiedUserArray = qualifiedUser.split("@");
                try {
                    username = qualifiedUserArray[0];
                    domain = qualifiedUserArray[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.trace("Couldn't parse domain from {}; trying without one",
                            qualifiedUser, e);
                }
            } else {
                username = qualifiedUser;
            }
            password = extractPassword(authenticationToken);

        } catch (NullPointerException e) {
            throw new AuthenticationException("fatal error decoding credentials", e);
        } catch (ClassCastException e) {
            throw new AuthenticationException("fatal error basic auth only", e);
        }
        ReadOnlyTransaction t = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<Users> iid = InstanceIdentifier.create(Authentication.class)
                .child(Users.class).builder().build();
        CheckedFuture<Optional<Users>, ReadFailedException> read = t.read(LogicalDatastoreType.CONFIGURATION, iid);
        try {
            Optional<Users> users = read.checkedGet();
            for(User user : users.get().getUsers()) {
                String userid = user.getUserid();
                if ((username + "@" + domain).equals(userid)) {
                    String ipassword = user.getPassword();
                    if (password.equals(ipassword)) {
                        ODLPrincipal p = ODLPrincipal.createODLPrincipal(username,"sdn",username+"@sdn", Sets.newHashSet("admin"));
                        return new SimpleAuthenticationInfo(p, password.toCharArray(), getName());
                    }
                }
            }
        } catch (Exception e) {

        }
        throw new AuthenticationException("unauthenticated");
    }

    /**
     * extract the username from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    static String extractUsername(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        return (String) authenticationToken.getPrincipal();
    }

    /**
     * extract the password from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    static String extractPassword(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        return new String(upt.getPassword());
    }

    /**
     * Since <code>TokenAuthRealm</code> is an <code>AuthorizingRealm</code>, it supports
     * individual steps for authentication and authorization.  In ODL's existing <code>TokenAuth</code>
     * mechanism, authentication and authorization are currently done in a single monolithic step.
     * <code>ODLPrincipal</code> is abstracted as a DTO between the two steps.  It fulfills the
     * responsibility of a <code>Principal</code>, since it contains identification information
     * but no credential information.
     *
     * @author Ryan Goulding (ryandgoulding@gmail.com)
     */
    private static class ODLPrincipal {

        private final String username;
        private final String domain;
        private final String userId;
        private final Set<String> roles;

        private ODLPrincipal(final String username, final String domain, final String userId, final Set<String> roles) {
            this.username = username;
            this.domain = domain;
            this.userId = userId;
            this.roles = roles;
        }

        /**
         * A static factory method to create <code>ODLPrincipal</code> instances.
         *
         * @param username The authenticated user
         * @param domain The domain <code>username</code> belongs to.
         * @param userId The unique key for <code>username</code>
         * @param roles The roles associated with <code>username</code>@<code>domain</code>
         * @return A Principal for the given session;  essentially a DTO.
         */
        static ODLPrincipal createODLPrincipal(final String username, final String domain,
                                                                                               final String userId, final Set<String> roles) {

            return new ODLPrincipal(username, domain, userId, roles);
        }

        /**
         * A static factory method to create <code>ODLPrincipal</code> instances.
         *
         * @param auth Contains identifying information for the particular request.
         * @return A Principal for the given session;  essentially a DTO.
         */
        static ODLPrincipal createODLPrincipal(final org.opendaylight.aaa.api.Authentication auth) {
            return createODLPrincipal(auth.user(), auth.domain(), auth.userId(), auth.roles());
        }

        String getUsername() {
            return this.username;
        }

        String getDomain() {
            return this.domain;
        }

        String getUserId() {
            return this.userId;
        }

        Set<String> getRoles() {
            return this.roles;
        }
    }
}
