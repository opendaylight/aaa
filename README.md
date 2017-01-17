## Opendaylight AAA

This project is aimed at providing a flexible, pluggable framework with out-of-the-box capabilities for Authentication, Authorization and Accounting.

## Caveats
The following caveats are applicable to the current AAA implementation:
 - The database (H2) used by ODL AAA Authentication store is not-cluster enabled. When deployed in a clustered environment each node needs to have its AAA
 user file synchronised using out of band means.

## Quick Start

### Building

*Prerequisite:*  The followings are required for building AAA:

- Maven 3.3.9+
- JDK8

Get the code:

    git clone https://git.opendaylight.org/gerrit/aaa

Build it:

    cd aaa && mvn clean install

### Installing

AAA is automatically installed upon installation of odl-restconf.  If you are using AAA from a non-RESTCONF context, you can install the necessary javax.servlet.Filter(s) through the following command:

	feature:install odl-aaa-shiro

### Protecting your REST/RestConf resources

Add the AAA `AAAShiroFilter` filter to your REST resource (RESTconf example):

    <servlet>
        <servlet-name>JAXRSRestconf</servlet-name>
        <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>org.opendaylight.controller.sal.rest.impl.RestconfApplication</param-value>
        </init-param>
        
        <!-- Token Auth Filter -->
        <init-param>
            <param-name>com.sun.jersey.spi.container.ContainerRequestFilters</param-name>
            <param-value>
                org.opendaylight.aaa.shiro.filters.AAAShiroFilter
            </param-value>
        </init-param>
        
        <load-on-startup>1</load-on-startup>
    </servlet>

Rebuild and re-install your REST resource. 

### Running

Once the installation finishes, one can authenticates with the Opendaylight controller by presenting a username/password and a domain name (scope) to be logged into:

    curl -s -d 'grant_type=password&username=admin&password=admin&scope=sdn' http://<controller>:<port>/oauth2/token

Upon successful authentication, the controller returns an access token with a configurable expiration in seconds, something similar to the followings:

    {"expires_in":3600,"token_type":"Bearer","access_token":"d772d85e-34c7-3099-bea5-cfafd3c747cb"}

The access token can then be used to access protected resources on the controller by passing it along in the standard HTTP Authorization header with the resource request.  Example:

    curl -s -H 'Authorization: Bearer d772d85e-34c7-3099-bea5-cfafd3c747cb' http://<controller>:<port>/restconf/operational/opendaylight-inventory:nodes

## Framework Overview

### Authentication

AAA supports 2 main authentication use-cases:  *direct* and *federated* authentication, with direct authentication being the simpler to deploy (i.e., no external system dependency) and hence being the out-of-the-box authentication mechanism.   

#### Direct

In this use-case, a user presents some credentials (e.g., username/password) directly to the Opendaylight (ODL) controller token endpoint `/oauth2/token` and receives an access token, which then can be used to access protected resources on the controller, similar to the example we saw in the Quickstart section: 

![](https://wiki.opendaylight.org/images/c/cc/Direct_authn.png)

#### Federated

In the federated use-case, the responsibility of authentication is delegated to a third-party IdP (perhaps, an enterprise-level IdP): 

![](https://wiki.opendaylight.org/images/f/fd/Federated_authn1.png)

In the above use-case, the user authenticates with a third-party IdP (username/password is shown as an example, but it could be anything that the IdP supports, such as MFA, OTP, etc...).  Upon successful authentication, the IdP  returns back a claim about the identity of that user.  The claim is then submitted to the Opendaylight token endpoint in exchange for an access token that can be used to access protected resources on the controller.  The IdP claim must be mapped into a corresponding ODL claim (user/domain/role) before an access token can be granted.

The Opendaylight controller comes with SSSD-based claim support, but other types of claim support can be also added with their implementation of the `org.opendaylight.aaa.api.ClaimAuth` API.

We can also take federation one step further and delegate token management and optionally part of the authorization responsibility to the third-party IdP:

![](https://wiki.opendaylight.org/images/2/22/Federated_authn2.png)

In this case, we use the IdP token directly as an access token to access protected resources on the controller.  The controller maintains only enough information needed for access control.  Validation of the token is performed by implementation of the `org.opendaylight.aaa.api.TokenAuth` API and can be daisy-chained as resource filters on the controller, with the last filter being the controller's built-in  `org.opendaylight.aaa.sts.DirectTokenAuthFilter` to properly register the authentication context.

### Authorization & Access Control

Authorization is implemented via the aaa-shiro modules.  Currently, authorization is limited purely to RESTCONF (HTTP) and does not focus on MD-SAL.

More information on how to configure authorization can be found on the Apache Shiro website:

    http://shiro.apache.org/web.html

### Accounting

Accounting is handled through the standard slf4j logging mechanisms used by the rest of OpenDaylight.  Thus, one can control logging verbosity through manipulating the log levels for individual packages and classes directly through the karaf shell, JMX, or etc/org.ops4j.pax.logging.cfg.  In normal operations, the default levels exposed do not provide much information about AAA services;  this is due to the fact that logging can severely degrade performance.

Two noteworthy logging activities are:
1) Enable debugging logging
2) Enable successful/unsuccessful authentication attempts logging

#### Enable Debugging Logging

For debugging purposes (i.e., to enable maximum verbosity), issue the following command:

karaf> log:set TRACE org.opendaylight.aaa

#### Enable Successful/Unsuccessful Authentication Attempts Logging
By default, successful/unsuccessful authentication attempts are NOT logged.  This is due to the fact that logging can severely decrease REST performance.  To enable logging of successful/unsuccessful REST attempts, issue the following command:

karaf> log:set DEBUG org.opendaylight.aaa.shiro.filters.AuthenticationListener

It is possible to add custom AuthenticationListener(s) to the Shiro based configuration, allowing different ways to listen for successful/unsuccessful authentication attempts.  Custom AuthenticationListener(s) must implement the org.apache.shiro.authc.AuthenticationListener interface.
