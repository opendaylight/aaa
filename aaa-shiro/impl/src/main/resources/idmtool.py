#!/usr/bin/env python

#
# Copyright (c) Brocade Communications 2016-2017, Lumina Networks 2018-2020 and others.
# All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

"""
idmtool

Used to manipulate ODL AAA idm on a node-per-node basis.  Assumes only one domain (sdn)
since current support in ODL is limited.

The best way to find out how to use this script is to invoke the normal argparse help:
> python idmtool -h

This script attempts to determine whether HTTP or HTTPS is used through reading the
org.ops4j.pax.web.cfg file, and determining whether HTTPS is enabled for the container.
"""
__copyright__ = "Copyright (c) 2016-2019 Brocade Communications Systems and others"
__credits__ = "Ryan Goulding"
__license__ = "EPL"
__version__ = "1.1"
__maintainer__ = "aaa-dev@lists.opendaylight.org"
__status__ = "Production"

import argparse
import getpass
import json
import os
import requests
import sys
import warnings

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
JOLOKIA_FILE_LOCATION = os.path.join(os.path.dirname(
    os.path.realpath(__file__)), "..", "etc", "org.jolokia.osgi.cfg")


def setup_http():
    """
    Sets the default port to try based on org.ops4j.pax.web.cfg.  If HTTPS is enabled then the script attempts
    to determine the port from org.osgi.service.http.port.secure.  If no port is specified (perfectly valid),
    then the script assumes the default (8443).  This functionality can still be overriden through specifying
    the --target-host argument during idmtool invocation.
    """

    try:
        pax_web_path = os.path.join(os.path.dirname(
            os.path.realpath(__file__)), "..", "etc", PAX_WEB_CFG_FILENAME)
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


user = ''
hostname = 'localhost'
port, protocol = setup_http()
target_host = '{}://{}:{}/'.format(protocol, hostname, port)
verifyCertificates = True

# main program arguments
parser.add_argument('user', help='username for ODL node', nargs=1)
parser.add_argument(
    '--target-host', help="target host url in form protocol://host:port", nargs=1)
parser.add_argument('-k', '--insecure',
                    help="disable HTTPS certificate verification", action='store_false')

subparsers = parser.add_subparsers(help='sub-command help')

# users table related
list_users = subparsers.add_parser('list-users', help='list all users')
list_users.set_defaults(func=list_users)
add_user = subparsers.add_parser('add-user', help='add a user')
add_user.set_defaults(func=add_user)
add_user.add_argument('new_user', help='new user name', nargs=1)
change_password = subparsers.add_parser(
    'change-password', help='change a password')
change_password.set_defaults(func=change_password)
change_password.add_argument(
    'userid', help='change the password for a particular userid', nargs=1)
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
get_grants = subparsers.add_parser(
    'get-grants', help='get grants for userid on sdn')
get_grants.set_defaults(func=get_grants)
get_grants.add_argument('userid', help="username@sdn", nargs=1)
delete_grant = subparsers.add_parser('delete-grant', help='delete a grant')
delete_grant.add_argument('userid', help='username@sdn', nargs=1)
delete_grant.add_argument('roleid', help='role@sdn', nargs=1)
delete_grant.set_defaults(func=delete_grant)

# jolokia password change related
change_jolokia_password = subparsers.add_parser(
    'change-jolokia-password', help='change the jolokia specific password')
change_jolokia_password.set_defaults(func=change_jolokia_password)


def process_result(r, print_output=True):
    """ Generic method to print result of a REST call """
    print('')
    success_status_codes = [200, 201, 202, 204]
    if r.status_code in success_status_codes:
        if print_output:
            print("Operation Successful!!")
            try:
                res = r.json()
                if res is not None:
                    print("json:")
                    print(json.dumps(res, indent=4, sort_keys=True))
            except ValueError:
                pass
        else:
            return r
    else:
        handle_errors(r.status_code)


def handle_errors(status_code):
    if 300 <= status_code < 400:
        print("Operation Failed")
        print("Redirection Error")
    elif 400 <= status_code < 500:
        print("Operation Failed")
        print("Client Error")
    elif status_code >= 500:
        print("Operation Failed")
        print("Server Error")
    print("Reason   :" + str(requests.status_codes._codes[status_code]))
    sys.exit(1)


def invoke_requests_method(req_method, url, print_output, **kwargs):
    try:
        response = req_method(url, **kwargs)
        response.raise_for_status()
    except Exception as err:
        handle_requests_exception(err)
    else:
        return process_result(response, print_output=print_output)


def handle_requests_exception(e):
    exception_type = type(e)
    if exception_type is requests.exceptions.HTTPError:
        print("HTTP error occurred: ", e)
    elif exception_type is requests.exceptions.ConnectionError:
        print("Connection Failed, Is the Controller running?")
        print("Connection Error: ", e)
    elif exception_type is requests.exceptions.ProxyError:
        print("Proxy Error, Please check Proxy: ", e)
    elif exception_type is requests.exceptions.SSLError:
        print("SSLError: ", e)
        print("SSL Error,check if HTTPS is configured properly")
        print("Also,to disable certificate verification, use the -k or --insecure flag")
    elif exception_type is requests.exceptions.Timeout:
        print("Timeout during Operation: ", e)
    elif exception_type is requests.exceptions.URLRequired:
        print("A valid URL is required: ", e)
    elif exception_type is requests.exceptions.TooManyRedirects:
        print("Too May Redirects when fetching the URL: ", e)
    elif exception_type is requests.exceptions.MissingSchema:
        print("The URL schema (e.g. http or https) is missing: ", e)
    elif exception_type is requests.exceptions.InvalidSchema:
        print("Schema is invalid: ", e)
    elif exception_type is requests.exceptions.InvalidURL:
        print("URL is invalid: ", e)
    elif exception_type is requests.exceptions.InvalidHeader:
        print("Header was invalid: ", e)
    elif exception_type is requests.exceptions.InvalidProxyURL:
        print("Invalid Proxy: ", e)
    elif exception_type is requests.exceptions.ChunkedEncodingError:
        print("Protocol Error in Encoding: ", e)
    elif exception_type is requests.exceptions.ContentDecodingError:
        print("Protocol Error in decoding: ", e)
    elif exception_type is requests.exceptions.StreamConsumedError:
        print("Protocol Error in handling data streams: ", e)
    elif exception_type is requests.exceptions.RetryError:
        print("Retries Failed: ", e)
    elif exception_type is requests.exceptions.UnrewindableBodyError:
        print("Protocol Error in handling data :", e)
    exit(1)


def get_request(user, password, url, description, output_result=True):
    if output_result:
        print(description)
        print(url)
    return invoke_requests_method(requests.get, url, output_result, auth=(user, password), verify=verifyCertificates)


def post_request(user, password, url, description, payload, headers):
    print(description)
    invoke_requests_method(requests.post, url, True, auth=(user, password), data=payload, headers=headers,
                           verify=verifyCertificates)


def post_request_unauthenticated(url, description, payload, headers, params=''):
    """
    Variation of POST without basic authentication
    """

    print(description)
    invoke_requests_method(requests.post, url, True, data=payload, headers=headers, verify=verifyCertificates,
                           params=params)


def put_request(user, password, url, description, payload, params):
    print(description)
    invoke_requests_method(requests.put, url, True, auth=(user, password), data=payload, headers=params,
                           verify=verifyCertificates)


def delete_request(user, password, url, description, payload='', params={'Content-Type': 'application/json'}):
    print(description)
    invoke_requests_method(requests.delete, url, True, auth=(user, password), data=payload, headers=params,
                           verify=verifyCertificates)


def poll_new_password():
    new_password = getpass.getpass(prompt="Enter new password: ")
    new_password_repeated = getpass.getpass(prompt="Re-enter password: ")
    if new_password != new_password_repeated:
        print("Passwords did not match;  cancelling the add_user request")
        sys.exit(1)
    return new_password


def list_users(user, password):
    get_request(user, password, target_host + 'auth/v1/users', 'list_users')


def add_user(user, password, new_user):
    new_password = poll_new_password()
    description = 'add_user({})'.format(user)
    url = target_host + 'auth/v1/users'
    payload = {'name': new_user, 'password': new_password,
               'description': '', "domainid": "sdn", 'email': ''}
    jsonpayload = json.dumps(payload)
    headers = {'Content-Type': 'application/json'}
    post_request(user, password, url, description, jsonpayload, headers)


def delete_user(user, password, userid):
    url = target_host + 'auth/v1/users/{}'.format(userid)
    description = 'delete_user({})'.format(userid)
    delete_request(user, password, url, description)


def change_password(user, password, existing_user_id):
    url = target_host + 'auth/v1/users/{}'.format(existing_user_id)
    r = get_request(user, password, target_host + 'auth/v1/users/{}'.format(existing_user_id), 'list_users',
                    output_result=False)
    existing = r.json()
    del existing['salt']
    del existing['password']
    new_password = poll_new_password()
    existing['password'] = new_password
    headers = {'Content-Type': 'application/json'}
    url = target_host + 'auth/v1/users/{}'.format(existing_user_id)
    put_request(user, password, url, 'change_password({})'.format(
        user), json.dumps(existing), headers)


def list_domains(user, password):
    get_request(user, password, target_host +
                'auth/v1/domains', 'list_domains')


def list_roles(user, password):
    get_request(user, password, target_host + 'auth/v1/roles', 'list_roles')


def add_role(user, password, role):
    url = target_host + 'auth/v1/roles'
    description = 'add_role({})'.format(role)
    payload = {'name': role, 'description': '', 'domainid': 'sdn'}
    data = json.dumps(payload)
    headers = {'Content-Type': 'application/json'}
    post_request(user, password, url, description, data, headers)


def delete_role(user, password, roleid):
    url = target_host + 'auth/v1/roles/{}'.format(roleid)
    description = 'delete_role({})'.format(roleid)
    delete_request(user, password, url, description)


def add_grant(user, password, userid, roleid):
    description = 'add_grant(userid={},roleid={})'.format(userid, roleid)
    payload = {"roleid": roleid, "userid": userid, "domainid": "sdn"}
    url = target_host + 'auth/v1/domains/sdn/users/{}/roles'.format(userid)
    data = json.dumps(payload)
    headers = {'Content-Type': 'application/json'}
    post_request(user, password, url, description, data, headers)


def get_grants(user, password, userid):
    get_request(user, password, target_host + 'auth/v1/domains/sdn/users/{}/roles'.format(userid),
                'get_grants({})'.format(userid))


def delete_grant(user, password, userid, roleid):
    url = target_host + \
        'auth/v1/domains/sdn/users/{}/roles/{}'.format(userid, roleid)
    print(url)
    description = 'delete_grant(userid={},roleid={})'.format(userid, roleid)
    delete_request(user, password, url, description)


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
                print("Successfully updated the jolokia password!")
            else:
                print(
                    "ERROR: Some unknown issue occurred while attempting to set the new password")
                sys.exit(1)
        else:
            print(
                "idmtool can only modify org.jolokia.osgi.cfg if authMode=basic at this time")
            sys.exit(1)
    except:
        print("Unable to change the jolokia password, please check configuration.")
        sys.exit(1)


args = parser.parse_args()
# python3 argparse has a bug [0] that does not catch the case of missing arguments
# in parse_args() unless you use required=True in add_subparsers(). But, that required
# argument is not available in python2. So, we'll catch that condition here to give
# a more clean error message to the user:
# [0] https://bugs.python.org/issue16308
try:
    a = getattr(args, "func")
except AttributeError:
    parser.print_help()
    sys.exit(1)
command = args.func.prog.split()[1:]

verifyCertificates = args.insecure
# disable SSL warning messages if --insecure option was chosen.
if not verifyCertificates:
    try:
        with warnings.catch_warnings():
            warnings.filterwarnings(
                "ignore", message=".*InsecurePlatformWarning.*")
    except:
        print("Unable to supress SSL warnings in this particular environment")
    print("Warning:  HTTPS certificate verification has been disabled.  Use at your own risk!")

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
    list_users(user, password)
if "list-domains" in command:
    list_domains(user, password)
if "list-roles" in command:
    list_roles(user, password)
if "add-user" in command:
    add_user(user, password, args.new_user[0])
if "add-grant" in command:
    add_grant(user, password, args.userid[0], args.roleid[0])
if "get-grants" in command:
    get_grants(user, password, args.userid[0])
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
if "change-jolokia-password" in command:
    change_jolokia_password()
