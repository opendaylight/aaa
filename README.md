[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.aaa/aaa-artifacts/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.aaa/aaa-artifacts)
[![Javadocs](https://www.javadoc.io/badge/org.opendaylight.aaa/aaa-docs.svg)](https://www.javadoc.io/doc/org.opendaylight.aaa/aaa-docs)
[![License](https://img.shields.io/badge/License-EPL%201.0-blue.svg)](https://opensource.org/licenses/EPL-1.0)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=coverage)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=opendaylight_aaa&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=opendaylight_aaa)

## Opendaylight AAA

This project is aimed at providing a flexible, pluggable framework with out-of-the-box capabilities for Authentication,
Authorization and Accounting (AAA).

## Caveats
The following caveats are applicable to the current AAA implementation:
 - The database (H2) used by ODL AAA Authentication store is not-cluster enabled. When deployed in a clustered
 environment each node contains unique local credentials.
 - AAA provides two local IdP Realm implementations; TokenAuthRealm and MdsalRealm.  Although the use of both Realms at
 the same time is possible through Shiro's multi-realm approach, it is considered bad practice to provide two local
 identity stores.  Thus, users should specify one or the other for $securityManager.realms entry in the aaa-app-config
 configuration.
 - The MdsalRealm is not initialized with any Users, Roles, Domains, or Grants.  The ability to add OOB Identity
 Information is considered separate work, and is targeted for the Fluorine release.

## Quick Start

### Building

*Prerequisite:*  The followings are required for building AAA:

- Maven 3.8.3+
- JDK 17
- Python 3.7+ (optional) for running wrapper scripts

Get the code:

Using HTTPS:
    git clone https://git.opendaylight.org/gerrit/aaa

USING SSH:
    git clone ssh://{USERNAME}@git.opendaylight.org:29418/aaa

Build it:

    cd aaa && mvn clean install

### Installing

AAA is automatically installed upon installation of odl-restconf-noauth and enabled through aaa-shiro-act.

If you are using AAA from a non-RESTCONF context, you can install the necessary javax.servlet.Filter(s) through the
following command:

	karaf> feature:install odl-aaa-shiro

### Running

Once the installation finishes, one can authenticate with the OpenDaylight controller by presenting a username/password
to access protected resources.
Example:

    curl -s -H 'Authorization: Basic YWRtaW46YWRtaW4=' \
    http://<controller>:<port>/rests/data/...?content=config

Upon successful authentication, session cookie will be created, which can be then used to access protected resources
during session, instead of providing username/password.
Example:

    curl -s -H 'Cookie: JSESSIONID=node0x12lwsvqbaxx15981soehtqed1.node0' \
    http://<controller>:<port>/rests/data/...?content=config


### Defaults

Although it is poor security practice, AAA's TokenAuthRealm creates some defaults out of the box.  In order to avoid
default credentials, please see the aaa-cli-jar module, which allows installers to pre-install identity information.
Due to the fact that OpenDaylight does not have a proper installer project, default credentials become a
chicken/egg problem.  The choice to utilize defaults was initially decided to help bootstrap interaction with ODL's
restful web services.  AAA's TokenAuthRealm creates:
* the "sdn" domain
* the "admin" and "user" roles
* the "admin" user with "admin" password
* 2 grants
  * admin user is granted admin role privileges on sdn domain
  * admin user is granted user role privileges on sdn domain

TokenAuthRealm's H2 file-based database, which stores the identity information, is secured with default credentials
"foo"/"bar".  Default credentials on the local file-based database is a smaller concern, since without running an H2
Server instance on the local machine (ODL doesn't by default), the database is only accessible locally (i.e., user in
front of keyboard).  However, these credentials can be adjusted too by setting "dbUsername" and "dbPassword" within
etc/org.opendaylight.aaa.h2.cfg.

## Framework Overview

### Authentication

AAA supports 2 main authentication use-cases:  *direct* and *federated* authentication, with direct authentication being
the simpler to deploy (i.e., no external system dependency) and hence being the out-of-the-box authentication mechanism.

#### Direct

In this use-case, a user presents some credentials (e.g., username/password) directly to the Opendaylight (ODL)
controller and receives a session cookie, which can be then used to access protected resources on the controller,
similar to the example we saw in the Quickstart section.

#### Federated

In the federated use-case, the responsibility of authentication is delegated to a third-party IdP (perhaps, an
enterprise-level IdP).

For more information, consult ODLJndiLdapRealm and ODLJndiLdapRealmAuthNOnly documentation.

### Authorization & Access Control

ODL supports two authorization engines at present, both of which are roughly similar in behavior.  Namely, the two
authorization engines are the MDSALDynamicAuthorizationFilter(1) and the RolesAuthorizationFilter(2).  For several
reasons explained further in this documentation, we STRONGLY encourage you to use the MDSALDyanmicAuthorizationFilter(1)
approach over the RolesAuthorizationFilter(2).

1) MDSALDyanmicAuthorizationFilter

The MDSALDynamicAuthorizationFilter is a mechanism used to restrict access to partcular URL endpoint patterns.  Users
may define a list of policies that are insertion-ordered.  Order matters for the list of policies, since the first
matching policy is applied.  This choice was made to emulate behavior of the Apache Shiro RolesAuthorizationFilter.

A policy is a key/value pair, where the key is a resource (i.e., a "url pattern") and the value is a list of permissions
for the resource.  The following describes the various elements of a policy:

resource:          The resource is a string url pattern as outlined by Apache Shiro.  For more information,
                   see http://shiro.apache.org/web.html.
description:       An optional description of the URL endpoint and why it is being secured.
permissions list:  A list of permissions for a particular policy.  If more than one permission exists in the permissions
                   list, the permissions are evaluted using logical "OR".

A permission describes the prerequisites to perform HTTP operations on a particular endpoint.  The following describes
the various elements of a permission:

role:              The role required to access the target URL endpoint.
actions list:      A leaf-list of HTTP permissions that are allowed for a Subject possessing the required role.

---------
Example:

To limit access to the modules endpoint, issue the following:

HTTP Operation:    put
URL:               /rests/data/aaa:http-authorization/policies
Headers:
    Content-Tye:       application/json
    Accept:            application/json

Body:
```json
{
  "aaa:policies": {
    "policies": [
      {
        "resource": "/rests/modules/**",
        "index": 1,
        "permissions": [
          {
            "role": "admin",
            "actions": [
              "get","post","put","patch","delete"
            ]
          }
        ]
      }
    ]
  }
}
```
--------
The above example locks down access to the modules endpoint (and any URLS available past modules) to the "admin" role.
Thus, an attempt from the OOB admin user will succeed with 2XX HTTP status code, while an attempt from the OOB "user"
user will fail with HTTP status code 401, as the "user" user is not granted the "admin" role.

NOTE:  "aaa:resource" value starts with "/rests".  Unlike the RolesAuthorizationFilter which is relative to the
ServletContext, The MDSALDyanmicAuthorizationFilter is relative to the Servlet Root (i.e., "/"). This is superior, as it
is more specific and does not allow for ambiguity.

2) aaa-app-config clustered application configuration "urls" section Authorization roles filter (i.e.,
"RolesAuthorizationFilter"). [DEPRECATED]

Authorization is implemented via the aaa-shiro modules.  RolesAuthorizationFilter (roles filter) is limited purely to
RESTCONF (HTTP) and does not focus on MD-SAL.

More information on how to configure authorization can be found on the Apache Shiro website: http://shiro.apache.org/web.html

NOTE:  Use of aaa-app-config.xml urls section to define roles requirements is discouraged!  This is due to the fact that
aaa-app-config.xml changes are only recognized on servlet container startup.  Changes to aaa-app-config.xml are only
honored upon restart.

NOTE:  Use of aaa-app-config.xml urls section to define roles requirements is discouraged!  This is due to the fact that
url patterns are matched relative to the servlet context.  This leaves room for ambiguity, since many endpoints may
match (i.e., "/rests/modules" and "/auth/modules" would both match a "/modules/**" rule).

### Accounting

Accounting is handled through the standard slf4j logging mechanisms used by the rest of OpenDaylight.  Thus, one can
control logging verbosity through manipulating the log levels for individual packages and classes directly through the
karaf shell, JMX, or etc/org.ops4j.pax.logging.cfg.  In normal operations, the default levels exposed do not provide
much information about AAA services;  this is due to the fact that logging can severely degrade performance.

Two noteworthy logging activities are:
1) Enable debugging logging
2) Enable successful/unsuccessful authentication attempts logging

#### Enable Debugging Logging

For debugging purposes (i.e., to enable maximum verbosity), issue the following command:

    karaf> log:set TRACE org.opendaylight.aaa

#### Enable Successful/Unsuccessful Authentication Attempts Logging
By default, successful/unsuccessful authentication attempts are NOT logged.  This is due to the fact that logging can
severely decrease REST performance.  To enable logging of successful/unsuccessful REST attempts, issue the following
command:

    karaf> log:set DEBUG org.opendaylight.aaa.shiro.filters.AuthenticationListener

It is possible to add custom AuthenticationListener(s) to the Shiro based configuration, allowing different ways to
listen for successful/unsuccessful authentication attempts.  Custom AuthenticationListener(s) must implement the
org.apache.shiro.authc.AuthenticationListener interface.
