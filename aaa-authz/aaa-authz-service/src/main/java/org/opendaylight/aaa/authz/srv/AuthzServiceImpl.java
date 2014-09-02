package org.opendaylight.aaa.authz.srv;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.controller.config.yang.config.aaa_authz.srv.Policies;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationResponseType;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import java.util.List;

/**
 * @author lmukkama
 *         Date: 9/2/14
 */
public class AuthzServiceImpl {


    private static List<Policies> listPolicies;

    private static final String WILDCARD_TOKEN = "*";

    public void setPolicies(List<Policies> policies){

        AuthzServiceImpl.listPolicies = policies;
    }

    public static AuthorizationResponseType reqAuthorization(ActionType actionType, LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier){

        AuthenticationService authenticationService = AuthzDomDataBroker.getInstance().getAuthService();

        if(authenticationService!=null && AuthzServiceImpl.listPolicies!=null && AuthzServiceImpl.listPolicies.size()>0){
            //Authentication Service exists. Can do authorization checks
            Authentication authentication = authenticationService.get();
            if(authentication!=null && authentication.roles()!=null && authentication.roles().size()>0){
                //Authentication claim object exists with atleast one role
                return checkAuthorization(actionType, authentication,logicalDatastoreType,yangInstanceIdentifier);

            }

        }

        return AuthorizationResponseType.Authorized;
    }


    private static AuthorizationResponseType checkAuthorization(ActionType actionType, Authentication authentication,LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier){

        for(Policies policy : AuthzServiceImpl.listPolicies){

            if(authentication.roles().contains(policy.getRole()) && policy.getResource().equals(yangInstanceIdentifier.toString()) && (policy.getAction().equals(WILDCARD_TOKEN) || actionType.equals(policy.getAction()))){
               return AuthorizationResponseType.Authorized;
            }

        }

        return AuthorizationResponseType.NotAuthorized;
    }

}
