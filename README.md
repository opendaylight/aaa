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

ODL supports two authorization engines at present, both of which are roughly similar in behavior.  Namely, the two authorization engines are the MDSALDynamicAuthorizationFilter(1) and the RolesAuthorizationFilter(2).  For several reasons explained further in this documentation, we STRONGLY encourage you to use the MDSALDyanmicAuthorizationFilter(1) approach over the RolesAuthorizationFilter(2).

1) MDSALDyanmicAuthorizationFilter

The MDSALDynamicAuthorizationFilter is a mechanism used to restrict access to partcular URL endpoint patterns.  Users may define a list of policies that are insertion-ordered.  Order matters for the list of policies, since the first matching policy is applied.  This choice was made to emulate behavior of the Apache Shiro RolesAuthorizationFilter.

A policy is a key/value pair, where the key is a resource (i.e., a "url pattern") and the value is a list of permissions for the resource.  The following describes the various elements of a policy:

resource:          The resource is a string url pattern as outlined by Apache Shiro.  For more information, see http://shiro.apache.org/web.html.
description:       An optional description of the URL endpoint and why it is being secured.
permissions list:  A list of permissions for a particular policy.  If more than one permission exists in the permissions list, the permissions are evaluted using logical "OR".

A permission describes the prerequisites to perform HTTP operations on a particular endpoint.  The following describes the various elements of a permission:

role:              The role required to access the target URL endpoint.
actions list:      A leaf-list of HTTP permissions that are allowed for a Subject possessing the required role.

---------
Example:

To limit access to the modules endpoint, issue the following:

HTTP Operation:    put
URL:               /restconf/config/aaa:http-authorization/policies
Headers:
    Content-Tye:       application/json
    Accept:            application/json

Body:

{
  "aaa:policies": {
    "aaa:policies": [
      {
        "aaa:resource": "/restconf/modules/**",
        "aaa:permissions": [
          {
            "aaa:role": "admin",
            "aaa:actions": [
              "get","post","put","patch","delete"
            ]
          }
        ]
      }
    ]
  }
}
--------
The above example locks down access to the modules endpoint (and any URLS available past modules) to the "admin" role.  Thus, an attempt from the OOB admin user will succeed with 2XX HTTP status code, while an attempt from the OOB "user" user will fail with HTTP status code 401, as the "user" user is not granted the "admin" role.

NOTE:  "aaa:resource" value starts with "/restconf".  Unlike the RolesAuthorizationFilter ("roles" in shiro.ini) which is relative to the ServletContext, The MDSALDyanmicAuthorizationFilter is relative to the Servlet Root (i.e., "/").  This is superior, as it is more specific and does not allow for ambiguity.

2) shiro.ini urls section Authorization roles filter (i.e., "RolesAuthorizationFilter"). [DEPRECATED]

Authorization is implemented via the aaa-shiro modules.  RolesAuthorizationFilter (roles filter) is limited purely to RESTCONF (HTTP) and does not focus on MD-SAL.

More information on how to configure authorization can be found on the Apache Shiro website:

    http://shiro.apache.org/web.html

NOTE:  Use of shiro.ini urls section to define roles requirements is discouraged!  This is due to the fact that shiro.ini changes are only recognized on servlet container startup.  Changes to shiro.ini are only honored upon restart.

NOTE:  Use of shiro.ini urls section to define roles requirements is discouraged!  This is due to the fact that url patterns are matched relative to the servlet context.  This leaves room for ambiguity, since many endpoints may match (i.e., "/restconf/modules" and "/auth/modules" would both match a "/modules/**" rule).

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
