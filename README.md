## Welcome to the Opendaylight AAA Project!

This project is aimed at providing a flexible, pluggable framework with out-of-the-box capabilities for:

* *Authentication*:  Means to authenticate the identity of both human and machine users (direct or federated).
* *Authorization*:  Means to authorize human or machine user access to resources including RPCs, notification subscriptions, and subsets of the datatree.
* *Accounting*:  Means to record and access the records of human or machine user access to resources including RPCs, notifications, and subsets of the datatree

## Quick Start

### Building

*Prerequisite:*  The followings are required for building AAA:

- Maven 3
- Java 7

Get the code:

    git clone https://git.opendaylight.org/gerrit/aaa

Build it:

    cd aaa && mvn clean install

### Installing

AAA installs into an existing Opendaylight controller Karaf installation.  If you don't have an Opendaylight installation, please refer to this [page](https://wiki.opendaylight.org/view/OpenDaylight_Controller:Installation).

Start the controller Karaf container:

	bin/karaf

Install AAA repository from the Karaf shell:

	repo-add mvn:org.opendaylight.aaa/features-aaa/0.1.0-SNAPSHOT/xml/features

Install all AAA features:

	feature:install odl-aaa-all

### Protecting your REST/RestConf resources

Add the AAA `TokeAuthFilter` filter to your REST resource (RESTconf example):

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
                org.opendaylight.aaa.sts.TokenAuthFilter
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

Here, the Opendaylight controller takes on 3 respective roles:

- *Identity Provider*:  Authenticates a user given some credentials.
- *Authorization Server*:  Determines what roles/permissions an authenticated user has.
- *Resource Provider*:  Provides access to a given resource based on the user's roles/permissions.

The built-in IdP for Opendaylight can be swapped out by a different implementation of the `org.opendaylight.aaa.api.CredentialAuth` API.

#### Federated

In the federated use-case, the responsibility of authentication is delegated to a third-party IdP (perhaps, an enterprise-level IdP): 

![](https://wiki.opendaylight.org/images/f/fd/Federated_authn1.png)

In the above use-case, the user authenticates with a third-party IdP (username/password is shown as an example, but it could be anything that the IdP supports, such as MFA, OTP, etc...).  Upon successful authentication, the IdP  returns back a claim about the identity of that user.  The claim is then submitted to the Opendaylight token endpoint in exchange for an access token that can be used to access protected resources on the controller.  The IdP claim must be mapped into a corresponding ODL claim (user/domain/role) before an access token can be granted.

The Opendaylight controller comes with SSSD-based claim support, but other types of claim support can be also added with their implementation of the `org.opendaylight.aaa.api.ClaimAuth` API.

We can also take federation one step further and delegate token management and optionally part of the authorization responsibility to the third-party IdP:

![](https://wiki.opendaylight.org/images/2/22/Federated_authn2.png)

In this case, we use the IdP token directly as an access token to access protected resources on the controller.  The controller maintains only enough information needed for access control.  Validation of the token is performed by implementation of the `org.opendaylight.aaa.api.TokenAuth` API and can be daisy-chained as resource filters on the controller, with the last filter being the controller's built-in  `org.opendaylight.aaa.sts.DirectTokenAuthFilter` to properly register the authentication context.

### Authorization & Access Control

Authorization is implemented via the aaa-authz modules, comprising of a yang based AuthZ policy schema, an MD-SAL AuthZ capable broker, an AuthZ
service engine invoked by the broker and executing policies.

Initially the AuthZ functionality is only able to handle RestConf requests, and to do so the Restconf connnector configuration must
 be explicitly modified as follows:

 0. Compile as per the above instructions
 1. If you have already run ODL with Restconf or the mdsal-all feature package under karaf, then proceed as per below. Alternatively skip to step 2.
 1a.  consider deleting the assembly/data directory in your karaf install. This will require the re-activation of features at karaf startup.
 1b. Delete the default restconf connector configuration file: "rm assembly/etc/opendaylight/karaf/10-rest-connector.xml"
 2. Start karaf and install the odl-aaa-all feature as per the previous instructions
 3. Start the odl-restconf feature via the command "feature:install odl-resctonf". An alternative can also be feature:install odl-mdsal-all
 
To unistall authz:
1. Unistall the feature via "feature:uninstall feature:odl-aaa-authz"
2. Either:
2a. Locate and open in an editor the default 10-rest-connector.xml configuration file in assembly/etc/opendaylight/karaf/.
     2. Change the <dom-broker> configuration element
        FROM:
                        <dom-broker>
                             <type xmlns:dom="urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom">dom:dom-broker-osgi-registry</type>
                             <name>authz-connector-default</name>
                         </dom-broker>
        TO:
                        <dom-broker>
                             <type xmlns:dom="urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom">dom:dom-broker-osgi-registry</type>
                             <name>dom-broker</name>
                         </dom-broker>
OR:
2b. Reinstall resctonf via the command "feature:install odl-resctonf"
 
Instructions for activating Authz in non karaf based ODL runtimes:
 1. Locate and open in an editor the default 10-rest-connector.xml configuration file. Default location is at 'configuration/initial'
 2. Change the <dom-broker> configuration element
    FROM:
                    <dom-broker>
                         <type xmlns:dom="urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom">dom:dom-broker-osgi-registry</type>
                         <name>dom-broker</name>
                     </dom-broker>
    TO:
                    <dom-broker>
                         <type xmlns:dom="urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom">dom:dom-broker-osgi-registry</type>
                         <name>authz-connector-default</name>
                     </dom-broker>
  3. Restart ODL

Default authorization are loaded from the configuration subsystem (TODO: Provide a default set)
They are accessible and editable via the restconf interface at: http://<odl address>/restconf/configuration/authorization-schema:simple-authorization/

The schema for policies is a list consisting of the following items:

  * Service : The application service that is the initiator of the request triggering an authorization check, eg Restconf.
  NOTE: The service field is currently not enforced, and a wildcard "*" is recommended.
  * Action: The action that is being authorized. Maps to one of: { create; read; update; delete; execute; subscribe; any }
  * Resource: The URI or Yang instance id of the resource, including wildcards (see examples below)
  * Role: The AuthN derived user role

Some examples of resources are
  Data : /operational/opendaylight-inventory:nodes/node/openflow:1/node-connector/openflow:1:1
  Wildcarded data: /configuration/opendaylight-inventory:nodes/node/*/node-connector/*
  RPC: /operations/example-ops:reboot
  Wildcarded RPC: /operations/example-ops:*
  Notification: /notifications/example-ops:startup

*More on MD-SAL authorization later...*

### Accounting  

*More on Accounting later...*
