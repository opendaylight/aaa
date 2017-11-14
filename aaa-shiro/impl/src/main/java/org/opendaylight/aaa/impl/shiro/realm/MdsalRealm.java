package org.opendaylight.aaa.shiro.realm;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.CheckedFuture;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.aaa.impl.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.aaa.impl.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Authentication;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Grant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Grants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Roles;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Users;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdsalRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory.getLogger(MdsalRealm.class);

    private static final InstanceIdentifier<Authentication> AUTH_IID =
            InstanceIdentifier.builder(Authentication.class).build();

    public MdsalRealm() {
        LOG.info("Instantiating {}", MdsalRealm.class.getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        final Set<String> authRoles = Sets.newHashSet();
        final ODLPrincipal odlPrincipal = (ODLPrincipal)principalCollection.getPrimaryPrincipal();
        final Optional<Authentication> opt = getAuthenticationContainer();
        if (opt.isPresent()) {
            final Authentication auth = opt.get();
            final Grants grants = auth.getGrants();
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.grants.Grants> grantsList = grants.getGrants();
            for (Grant grant : grantsList ) {
                if (grant.getUserid().equals(odlPrincipal.getUserId())) {
                    final Roles roles = auth.getRoles();
                    final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.roles.Roles> rolesList = roles.getRoles();
                    for (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.roles.Roles role : rolesList) {
                        if (role.getRoleid().equals(grant.getRoleid())) {
                            authRoles.add(role.getRoleid());
                        }
                    }
                }
            }
        }
        return new SimpleAuthorizationInfo(authRoles);
    }

    private Optional<Authentication> getAuthenticationContainer() {
        final DataBroker dataBroker = AAAShiroProvider.getInstance().getDataBroker();
        try (final ReadOnlyTransaction ro = dataBroker.newReadOnlyTransaction()) {
            final CheckedFuture<Optional<Authentication>, ReadFailedException> result =
                    ro.read(LogicalDatastoreType.CONFIGURATION, AUTH_IID);

            final Optional<Authentication> authentication = result.get();
            return authentication;
        } catch (final InterruptedException | ExecutionException e) {
            LOG.error("Couldn't access authentication container", e);
        }
        return Optional.absent();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        final String username = TokenUtils.extractUsername(authenticationToken);
        final Optional<Authentication> opt = getAuthenticationContainer();
        if (opt.isPresent()) {
            final Authentication auth = opt.get();
            final Users users = auth.getUsers();
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.users.Users> usersList =
                    users.getUsers();
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.users.Users u : usersList) {
                final String inputUsername = HeaderUtils.extractUsername(username);
                final String domainId = HeaderUtils.extractDomain(username);
                final String inputUserId = String.format("%s@%s", inputUsername, domainId);
                if(u.getUserid().equals(inputUserId)) {
                    final String inputPassword = TokenUtils.extractPassword(authenticationToken);
                    final String hashedInputPassword = SHA256Calculator.getSHA256(inputPassword, u.getSalt());
                    if (hashedInputPassword.equals(u.getPassword())) {
                        final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal("ryan","sdn","ryan@sdn",Sets.newHashSet("admin"));
                        return new SimpleAuthenticationInfo(odlPrincipal, inputPassword, getName());
                    }
                }
            }
        }
        throw new AuthenticationException(String.format("Couldn't authenticate %s", username));
    }
}
