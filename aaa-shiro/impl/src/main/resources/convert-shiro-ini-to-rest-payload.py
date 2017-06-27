#!/bin/env python

#
# Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

#
# convert-shiro-ini-to-rest-payload.py
#
# Used to help ease upgrades.  In ODL Nitrogen, AAA related application config
# is now done via the datastore.  This allows a more cohesive experience in
# line with the rest of the controller architecture.  More information about
# this can be found here:
#
# https://bugs.opendaylight.org/show_bug.cgi?id=7793
#
# This program assumes a correctly formatted shiro.ini file.  No extra checks
# are done on shiro.ini.
#

import sys, ConfigParser
from xml.etree.ElementTree import Element, SubElement, tostring
from xml.dom import minidom


SHIRO_CONFIGURATION = "shiro-configuration"
NS = "urn:opendaylight:aaa:app:config"
MAIN_SECTION = "main"
URLS_SECTION = "urls"

def convert(filename):
    '''
    convert shiro.ini to a XML based representation
    '''
    config = ConfigParser.ConfigParser()
    config.optionxform = str
    config.read(filename)
    root = Element(SHIRO_CONFIGURATION)
    root.attrib['xmlns'] = NS
    for section in config.sections():
        if MAIN_SECTION in section or URLS_SECTION in section:
            for item in config.items(section):
                child = SubElement(root, section)
                k = SubElement(child, "pair-key")
                k.text = item[0]
                v = SubElement(child, "pair-value")
                v.text = item[1]
    return root

def usage():
    print "Usage:"
    print "> python convert-shiro-ini-to-rest <filename>"

if __name__ == '__main__':
    try:
        filename = sys.argv[1]
        et = convert(filename)
        xmlstr = minidom.parseString(tostring(et)).toprettyxml(indent="    ")
        print xmlstr
    except(IndexError):
        usage()
