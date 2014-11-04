package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store;


import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.core.api.Broker;
import org.opendaylight.controller.sal.core.api.Consumer;
import org.opendaylight.controller.sal.core.api.Provider;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Tokencache;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.TokencacheBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.claims.AuthorizationBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

  /**
   * Created by wdec on 26/08/2014.
   */

/**
 * Created by wdec on 31/10/2014.
 */

  public class AuthNStore implements AutoCloseable, Provider {

    private DataBroker broker;
    private Broker.ProviderSession providerSession;
    private AuthenticationService authenticationService;

    public void setBroker(DataBroker dataBrokerService) {
      this.broker = dataBrokerService;
    }

    @Override
    public void close() throws Exception {

    }
    
    private Tokencache buildTokencache() {
        
        AuthorizationBuilder authorizationBuilder = new AuthorizationBuilder();
        authorizationBuilder.setExpiration(256L);
        
        ClaimsKey claimsKey = new ClaimsKey("KK0001");
        
        ClaimsBuilder claimsBuilder = new ClaimsBuilder();
        claimsBuilder.setAuthorization(authorizationBuilder.build());
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
    
    // Implements AuthzBroker handling of registering consumers or providers.
    @Override
    public void onSessionInitiated(Broker.ProviderSession providerSession) {

      //Get now the real DOMDataBroker and register it with the AuthzDOMBroker together with the provider session
      final DOMDataBroker domDataBroker = providerSession.getService(DOMDataBroker.class);
      writeTokenToDatastore();
    }

    @Override
    public Collection<ProviderFunctionality> getProviderFunctionality() {
      return null;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
      this.authenticationService = authenticationService;
    }

    //Wrapper for Provider

    public static class ProviderWrapper implements Provider{
      private  final Provider provider;


      public ProviderWrapper(Provider provider) {
        this.provider = provider;
      }

      @Override
      public void onSessionInitiated(Broker.ProviderSession providerSession) {
        //Do a Noop when the real broker calls back
      }

      @Override
      public Collection<ProviderFunctionality> getProviderFunctionality() {
        //Allow the RestconfImpl to respond to this
        return provider.getProviderFunctionality();
      }
    }

    //Wrapper for Consumer
    public static class ConsumerWrapper implements Consumer {

      private final Consumer consumer;

      public ConsumerWrapper(Consumer consumer) {
        this.consumer = consumer;
      }

      @Override
      public void onSessionInitiated(Broker.ConsumerSession consumerSession) {
        //Do a Noop when the real broker calls back
      }

      @Override
      public Collection<ConsumerFunctionality> getConsumerFunctionality() {
        return consumer.getConsumerFunctionality();
      }
    }
  }

