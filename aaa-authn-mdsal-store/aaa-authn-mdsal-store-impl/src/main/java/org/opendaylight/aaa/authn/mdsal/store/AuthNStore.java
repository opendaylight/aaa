package org.opendaylight.aaa.authn.mdsal.store;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Tokencache;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.TokencacheBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsKey;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

  /**
   * Created by wdec on 26/08/2014.
   */

/**
 * Created by wdec on 31/10/2014.
 */

  public class AuthNStore implements AutoCloseable, TokenStore {

    private DataBroker broker;
    private Broker.ProviderSession providerSession;
    private AuthenticationService authenticationService;
    private BigInteger timeToLive;

  public void setBroker(DataBroker dataBrokerService) {
      this.broker = dataBrokerService;
    }
  public void setTimeToLive(BigInteger timeToLive) {
    this.timeToLive = timeToLive;
  }

    @Override
    public void close() throws Exception {

    }
    
    private Tokencache buildTokencache() {
        


        
        ClaimsKey claimsKey = new ClaimsKey("KK0001", null);
        
        ClaimsBuilder claimsBuilder = new ClaimsBuilder();
        claimsBuilder.setExpiration(256L);
        claimsBuilder.setClientId("AA001");
        claimsBuilder.setDomain("AAA");
        claimsBuilder.setKey(claimsKey);
        List<String> roles = new ArrayList<String>();
        roles.add("User");
        claimsBuilder.setRoles(roles);
        claimsBuilder.setToken("TOK0001");
        claimsBuilder.setUser("JohnDoe");
        claimsBuilder.setUserId("U001");
        List<Claims> claims = new ArrayList<Claims>();
        claims.add(claimsBuilder.build());
        
        TokencacheBuilder tokencacheBuilder = new TokencacheBuilder();
        tokencacheBuilder.setClaims(claims);
        return tokencacheBuilder.build();
    }
    
    private void writeTokenToDatastore() {
        final InstanceIdentifier<Tokencache>  tokencache_iid = InstanceIdentifier.builder(Tokencache.class).build();
        
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.put( LogicalDatastoreType.OPERATIONAL,tokencache_iid, buildTokencache());
        
        ListenableFuture<RpcResult<TransactionStatus>> commitFuture = tx.commit();
        
        Futures.addCallback( commitFuture, new FutureCallback<RpcResult<TransactionStatus>>() {
            @Override
            public void onSuccess( RpcResult<TransactionStatus> result ) {
                if( result.getResult() != TransactionStatus.COMMITED ) {
                }
                
                notifyCallback( result.getResult() == TransactionStatus.COMMITED );
            }
            
            @Override
            public void onFailure( Throwable t ) {
                // We shouldn't get an OptimisticLockFailedException (or any ex) as no
                // other component should be updating the operational state.
                
                notifyCallback( false );
            }
            
            void notifyCallback( boolean result ) {
//                if( resultCallback != null ) {
//                    resultCallback.apply( result );
//                }
            }
        } );
    }


    public void setAuthenticationService(AuthenticationService authenticationService) {
      this.authenticationService = authenticationService;
    }


  @Override
  public void put(String token, Authentication auth) {

    writeTokenToDatastore();
  }

  @Override
  public Authentication get(String token) {
    return null;
  }

  @Override
  public boolean delete(String token) {
    return false;
  }

  @Override
  public long tokenExpiration() {
    return 0;
  }


}

