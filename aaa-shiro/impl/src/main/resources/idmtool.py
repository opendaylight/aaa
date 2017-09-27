#!/usr/bin/env python

#
# Copyright (c) 2016 Brocade Communications Systems and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

'''
idmtool

Used to manipulate ODL AAA idm on a node-per-node basis.  Assumes only one domain (sdn)
since current support in ODL is limited.

The best way to find out how to use this script is to invoke the normal argparse help:
> python idmtool -h

This script attempts to determine whether HTTP or HTTPS is used through reading the
org.ops4j.pax.web.cfg file, and determining whether HTTPS is enabled for the container.
'''

__author__ = "Ryan Goulding"
__copyright__ = "Copyright (c) 2016 Brocade Communications Systems and others"
__credits__ = "Ryan Goulding"
__license__ = "EPL"
__version__ = "1.1"
__maintainer__ = "Ryan Goulding"
__email__ = "ryandgoulding@gmail.com"
__status__ = "Production"

import argparse, getpass, json, os, requests, sys, warnings

parser = argparse.ArgumentParser('idmtool')


# Constants used to peek into the pax web config.  This is useful to determine whether HTTPS is enabled.
PAX_WEB_CFG_FILENAME = 'org.ops4j.pax.web.cfg'
HTTP_SECURE_ENABLED_KEY = 'org.osgi.service.http.secure.enabled'
HTTP_PORT_SECURE_KEY = 'org.osgi.service.http.port.secure'
DEFAULT_HTTP_PORT_SECURE = '8443'
DEFAULT_HTTP_PORT = '8181'
HTTP_PORT_KEY = 'org.osgi.service.http.port'
DEFAULT_PROTOCOL = 'http'
HTTPS_PROTOCOL = 'https'

# Jolokia related constants
JOLOKIA_FILE_LOCATION = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..", "etc", "org.jolokia.osgi.cfg")

def setup_http():
    '''
    Sets the default port to try based on org.ops4j.pax.web.cfg.  If HTTPS is enabled then the script attempts
    to determine the port from org.osgi.service.http.port.secure.  If no port is specified (perfectly valid),
    then the script assumes the default (8443).  This functionality can still be overriden through specifying
    the --target-host argument during idmtool invocation.
    '''

    try:
        pax_web_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..", "etc", PAX_WEB_CFG_FILENAME)
        with open(pax_web_path) as f:
            # build up a dictionary of the properties specified in the pax config
            content = f.readlines()
            d = dict()
            for line in content:
                if '=' in line and not line.startswith('#'):
                    pair = line.split('=')
                    loperand = pair[0].strip()
                    roperand = pair[1].strip()
                    d[loperand] = roperand

            # if HTTPS is enabled, return the secure port (if it is specified), or 8443 if it is not specified.
            http_secure_enabled = d.get(HTTP_SECURE_ENABLED_KEY)
            if http_secure_enabled is not None and 'true' in http_secure_enabled:
                return d.get(HTTP_PORT_SECURE_KEY, DEFAULT_HTTP_PORT_SECURE), HTTPS_PROTOCOL
            else:
                return DEFAULT_HTTP_PORT, DEFAULT_PROTOCOL
    except IOError:
        return DEFAULT_HTTP_PORT, DEFAULT_PROTOCOL


user=''
hostname='localhost'
port,protocol=setup_http()
target_host='{}://{}:{}/'.format(protocol, hostname, port)
verifyCertificates=True

# main program arguments
parser.add_argument('user',help='username for ODL node', nargs=1)
parser.add_argument('--target-host', help="target host url in form protocol://host:port", nargs=1)
parser.add_argument('-k', '--insecure', help="disable HTTPS certificate verification", action='store_false')

subparsers = parser.add_subparsers(help='sub-command help')

# users table related
list_users = subparsers.add_parser('list-users', help='list all users')
list_users.set_defaults(func=list_users)
add_user = subparsers.add_parser('add-user', help='add a user')
add_user.set_defaults(func=add_user)
add_user.add_argument('newUser', help='new user name', nargs=1)
change_password = subparsers.add_parser('change-password', help='change a password')
change_password.set_defaults(func=change_password)
change_password.add_argument('userid', help='change the password for a particular userid', nargs=1)
delete_user = subparsers.add_parser('delete-user', help='delete a user')
delete_user.add_argument('userid', help='name@sdn', nargs=1)
delete_user.set_defaults(func=delete_user)

# domains table related
# only read is defined;  this was done on purpose since the "domain" concept
# is mostly unsupported in ODL.
list_domains = subparsers.add_parser('list-domains', help='list all domains')
list_domains.set_defaults(func=list_domains)

# roles table related
list_roles = subparsers.add_parser('list-roles', help='list all roles')
list_roles.set_defaults(func=list_roles)
add_role = subparsers.add_parser('add-role', help='add a role')
add_role.add_argument('role', help='role name', nargs=1)
add_role.set_defaults(func=add_role)
delete_role = subparsers.add_parser('delete-role', help='delete a role')
delete_role.add_argument('roleid', help='rolename@sdn', nargs=1)
delete_role.set_defaults(func=delete_role)
add_grant = subparsers.add_parser('add-grant', help='add a grant')
add_grant.set_defaults(func=add_grant)
add_grant.add_argument('userid', help="username@sdn", nargs=1)
add_grant.add_argument('roleid', help="role@sdn", nargs=1)
get_grants = subparsers.add_parser('get-grants', help='get grants for userid on sdn')
get_grants.set_defaults(func=get_grants)
get_grants.add_argument('userid', help="username@sdn", nargs=1)
delete_grant = subparsers.add_parser('delete-grant', help='delete a grant')
delete_grant.add_argument('userid', help='username@sdn', nargs=1)
delete_grant.add_argument('roleid', help='role@sdn', nargs=1)
delete_grant.set_defaults(func=delete_grant)

# OAuth2 Token retrieval
get_oauth2_token = subparsers.add_parser('get-oauth2-token', help='get an OAuth2 token')
get_oauth2_token.add_argument('scope', help='the domain for the token scope', nargs=1)
get_oauth2_token.set_defaults(func=get_oauth2_token)

# jolokia password change related
change_jolokia_password = subparsers.add_parser('change-jolokia-password', help='change the jolokia specific password')
change_jolokia_password.set_defaults(func=change_jolokia_password)

def process_result(r):
    ''' Generic method to print result of a REST call '''
    print ''
    sc = r.status_code
    if sc >= 200 and sc < 300:
        print "command succeeded!"
        try:
            res = r.json()
            if res is not None:
                print '\njson:\n', json.dumps(res, indent=4, sort_keys=True)
        except(ValueError):
            pass
    elif sc == 401:
        print "Incorrect Credentials Provided"
    elif sc == 404:
        print "RESTconf is either not installed or not initialized yet"
    elif sc >= 500 and sc < 600:
        print "Internal Server Error Ocurred"
    else:
        print "Unknown error; HTTP status code: {}".format(sc)

def handle_exception(e):
    exceptionType = type(e)
    if exceptionType is requests.exceptions.SSLError:
        print "requests.exception.SSLError: Is HTTPS configured correctly?  To disable certificate verification, use the -k or --insecure flag"
    else:
        print "Unable to connect; are you sure the controller is up?"
    sys.exit(1)

def get_request(user, password, url, description, outputResult=True):
    if outputResult:
        print description
    try:
        r = requests.get(url, auth=(user,password), verify=verifyCertificates)
        if outputResult:
            process_result(r)
        return r
    except requests.exceptions.ConnectionError as e:
        if outputResult:
            handle_exception(e)
        sys.exit(1)

def post_request(user, password, url, description, payload, headers):
    print description
    try:
        r = requests.post(url, auth=(user,password), data=payload, headers=headers, verify=verifyCertificates)
        process_result(r)
    except requests.exceptions.ConnectionError as e:
        handle_exception(e)

def post_request_unauthenticated(url, description, payload, headers, params=''):
    '''
    Variation of POST without basic authentication
    '''

    print description
    try:
        r = requests.post(url, data=payload, headers=headers, verify=verifyCertificates,params=params)
        process_result(r)
    except requests.exceptions.ConnectionError as e:
        handle_exception(e)

def put_request(user, password, url, description, payload, params):
    print description
    try:
        r = requests.put(url, auth=(user,password), data=payload, headers=params, verify=verifyCertificates)
        process_result(r)
    except requests.exceptions.ConnectionError as e:
        handle_exception(e)

def delete_request(user, password, url, description, payload='', params={'Content-Type':'application/json'}):
    print description
    try:
        r = requests.delete(url, auth=(user,password), data=payload, headers=params, verify=verifyCertificates)
        process_result(r)
    except requests.exceptions.ConnectionError as e:
        handle_exception(e)

def poll_new_password():
    new_password = getpass.getpass(prompt="Enter new password: ")
    new_password_repeated = getpass.getpass(prompt="Re-enter password: ")
    if new_password != new_password_repeated:
        print "Passwords did not match;  cancelling the add_user request"
        sys.exit(1)
    return new_password

def list_users(user, password):
    get_request(user, password, target_host + 'auth/v1/users', 'list_users')

def add_user(user, password, newUser):
    new_password = poll_new_password()
    description = 'add_user({})'.format(user)
    url = target_host + 'auth/v1/users'
    payload =  {'name':newUser, 'password':new_password, 'description':'', "domainid":"sdn", 'email':''}
    jsonpayload = json.dumps(payload)
    headers={'Content-Type':'application/json'}
    post_request(user, password, url, description, jsonpayload, headers)

def delete_user(user, password, userid):
    url = target_host + 'auth/v1/users/{}'.format(userid)
    description = 'delete_user({})'.format(userid)
    delete_request(user, password, url, description)

def change_password(user, password, existingUserId):
    url = target_host + 'auth/v1/users/{}'.format(existingUserId)
    r = get_request(user, password, target_host + 'auth/v1/users/{}'.format(existingUserId), 'list_users', outputResult=False)
    try:
        existing = r.json()
        del existing['salt']
        del existing['password']
        new_password = poll_new_password()
        existing['password'] = new_password
        description='change_password({})'.format(existingUserId)
        headers={'Content-Type':'application/json'}
        url = target_host + 'auth/v1/users/{}'.format(existingUserId)
        put_request(user, password, url, 'change_password({})'.format(user), json.dumps(existing), headers)
    except(AttributeError):
        print "Unable to connect;  are you sure the controller is up?"
        sys.exit(1)

def list_domains(user, password):
    get_request(user, password, target_host + 'auth/v1/domains', 'list_domains')

def list_roles(user, password):
    get_request(user, password, target_host + 'auth/v1/roles', 'list_roles')

def add_role(user, password, role):
    url = target_host + 'auth/v1/roles'
    description = 'add_role({})'.format(role)
    payload = {'name':role, 'description':'', 'domainid':'sdn'}
    data = json.dumps(payload)
    headers={'Content-Type':'application/json'}
    post_request(user, password, url, description, data, headers)

def delete_role(user, password, roleid):
    url = target_host + 'auth/v1/roles/{}'.format(roleid)
    description = 'delete_role({})'.format(roleid)
    delete_request(user, password, url, description)

def add_grant(user, password, userid, roleid):
    description = 'add_grant(userid={},roleid={})'.format(userid, roleid)
    payload = {"roleid":roleid, "userid":userid, "domainid":"sdn"}
    url = target_host + 'auth/v1/domains/sdn/users/{}/roles'.format(userid)
    data=json.dumps(payload)
    headers={'Content-Type':'application/json'}
    post_request(user, password, url, description, data, headers)

def get_grants(user, password, userid):
    get_request(user, password, target_host + 'auth/v1/domains/sdn/users/{}/roles'.format(userid), 'get_grants({})'.format(userid))

def delete_grant(user, password, userid, roleid):
    url = target_host + 'auth/v1/domains/sdn/users/{}/roles/{}'.format(userid, roleid)
    print url
    description = 'delete_grant(userid={},roleid={})'.format(userid, roleid)
    delete_request(user, password, url, description)

def get_oauth2_token(user, password, scope):
    url = target_host + 'oauth2/token'
    print url
    description = 'get_oauth2_token(scope={})'.format(scope)
    params = 'grant_type=password&username={}&password={}&scope={}'.format(user, password, scope)
    payload = {}
    headers = {'Content-Type':'application/x-www-form-urlencoded'}
    post_request_unauthenticated(url, description, payload, headers, params)

def change_jolokia_password():
    try:
        with open(JOLOKIA_FILE_LOCATION, "r") as f:
            lines = f.readlines()
        basic_mode = False
        for line in lines:
            if "authMode" in line:
                if "basic" in line and "#" not in line:
                    basic_mode = True
        if basic_mode:
            replaced = False
            new_password = poll_new_password()
            with open(JOLOKIA_FILE_LOCATION, "w") as f:
                for line in lines:
                    if "password" in line:
                        f.write('password={}'.format(new_password))
                        replaced = True
                    else:
                        f.write(line)
            f.close()
            if replaced:
                print "Successfully updated the jolokia password!"
            else:
                print "ERROR: Some unknown issue occurred while attempting to set the new password"
                sys.exit(1)
        else:
            print "idmtool can only modify org.jolokia.osgi.cfg if authMode=basic at this time"
            sys.exit(1)
    except:
        print "Unable to change the jolokia password, please check configuration."
        sys.exit(1)

args = parser.parse_args()
command = args.func.prog.split()[1:]


verifyCertificates = args.insecure
# disable SSL warning messages if --insecure option was chosen.
if not verifyCertificates:
    try:
        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", message=".*InsecurePlatformWarning.*")
    except:
        print "Unable to supress SSL warnings in this particular environment"
    print "Warning:  HTTPS certificate verification has been disabled.  Use at your own risk!"

user = args.user[0]
password = getpass.getpass()
temp_host_arr = args.target_host
if temp_host_arr is not None:
    temp_host_val = temp_host_arr[0]
    if temp_host_val is not None:
        target_host = temp_host_val
        if not target_host.endswith("/"):
            target_host += "/"

if "list-users" in command:
    list_users(user,password)
if "list-domains" in command:
    list_domains(user,password)
if "list-roles" in command:
    list_roles(user,password)
if "add-user" in command:
    add_user(user,password, args.newUser[0])
if "add-grant" in command:
    add_grant(user,password, args.userid[0], args.roleid[0])
if "get-grants" in command:
    get_grants(user,password, args.userid[0])
if "change-password" in command:
    change_password(user, password, args.userid[0])
if "delete-user" in command:
    delete_user(user, password, args.userid[0])
if "delete-role" in command:
    delete_role(user, password, args.roleid[0])
if "add-role" in command:
    add_role(user, password, args.role[0])
if "delete-grant" in command:
    delete_grant(user, password, args.userid[0], args.roleid[0])
if "get-oauth2-token" in command:
    get_oauth2_token(user, password, args.scope[0])
if "change-jolokia-password" in command:
    change_jolokia_password()
