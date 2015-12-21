/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.authn.mdsal.store;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.GrantBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.RoleBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.UserBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
/**
 *
 * @author saichler@gmail.com
 *
 * This class is a codec to convert between MDSAL objects and IDM model objects. It is doing so via reflection when it assumes that the MDSAL
 * Object and the IDM model object has the same method names.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sharon Aicler - saichler@cisco.com
 *
 */
public abstract class IDMObject2MDSAL {
    private static final Logger LOG = LoggerFactory.getLogger(IDMObject2MDSAL.class);
    // this is a Map mapping between the class type of the IDM Model object to a
    // structure containing the corresponding setters and getter methods
    // in MDSAL object
    private static Map<Class<?>, ConvertionMethods> typesMethods = new HashMap<Class<?>, ConvertionMethods>();

    // This method generically via reflection receive a MDSAL object and the
    // corresponding IDM model object class type and
    // creates an IDM model element from the MDSAL element
    private static Object fromMDSALObject(Object mdsalObject, Class<?> type) throws Exception {
        if (mdsalObject == null)
            return null;
        Object result = type.newInstance();
        ConvertionMethods cm = typesMethods.get(type);
        if (cm == null) {
            cm = new ConvertionMethods();
            typesMethods.put(type, cm);
            Method methods[] = type.getMethods();
            for (Method m : methods) {
                if (m.getName().startsWith("set")) {
                    cm.setMethods.add(m);
                    Method gm = null;
                    if (m.getParameterTypes()[0].equals(Boolean.class)
                            || m.getParameterTypes()[0].equals(boolean.class))
                        gm = ((DataObject) mdsalObject).getImplementedInterface().getMethod(
                                "is" + m.getName().substring(3), (Class[]) null);
                    else {
                        try {
                            gm = ((DataObject) mdsalObject).getImplementedInterface().getMethod(
                                    "get" + m.getName().substring(3), (Class[]) null);
                        } catch (Exception err) {
                            LOG.error("Error associating get call", err);
                        }
                    }
                    cm.getMethods.put(m.getName(), gm);
                }
            }
        }
        for (Method m : cm.setMethods) {
            try {
                m.invoke(
                        result,
                        new Object[] { cm.getMethods.get(m.getName()).invoke(mdsalObject,
                                (Object[]) null) });
            } catch (Exception err) {
                LOG.error("Error invoking reflection method", err);
            }
        }
        return result;
    }

    // This method generically use reflection to receive an IDM model object and
    // the corresponsing MDSAL object and creates
    // a MDSAL object out of the IDM model object
    private static Object toMDSALObject(Object object, Class<?> mdSalBuilderType) throws Exception {
        if (object == null)
            return null;
        Object result = mdSalBuilderType.newInstance();
        ConvertionMethods cm = typesMethods.get(mdSalBuilderType);
        if (cm == null) {
            cm = new ConvertionMethods();
            typesMethods.put(mdSalBuilderType, cm);
            Method methods[] = mdSalBuilderType.getMethods();
            for (Method m : methods) {
                if (m.getName().startsWith("set")) {
                    try {
                        Method gm = null;
                        if (m.getParameterTypes()[0].equals(Boolean.class)
                                || m.getParameterTypes()[0].equals(boolean.class))
                            gm = object.getClass().getMethod("is" + m.getName().substring(3),
                                    (Class[]) null);
                        else
                            gm = object.getClass().getMethod("get" + m.getName().substring(3),
                                    (Class[]) null);
                        cm.getMethods.put(m.getName(), gm);
                        cm.setMethods.add(m);
                    } catch (NoSuchMethodException err) {
                    }
                }
            }
            cm.builderMethod = mdSalBuilderType.getMethod("build", (Class[]) null);
        }
        for (Method m : cm.setMethods) {
            m.invoke(result,
                    new Object[] { cm.getMethods.get(m.getName()).invoke(object, (Object[]) null) });
        }

        return cm.builderMethod.invoke(result, (Object[]) null);
    }

    // A struccture class to hold the getters & setters of each type to speed
    // things up
    private static class ConvertionMethods {
        private List<Method> setMethods = new ArrayList<Method>();
        private Map<String, Method> getMethods = new HashMap<String, Method>();
        private Method builderMethod = null;
    }

    // Convert Domain
    public static org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain toMDSALDomain(
            Domain domain) {
        try {
            return (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain) toMDSALObject(
                    domain, DomainBuilder.class);
        } catch (Exception err) {
            LOG.error("Error converting domain to MDSAL object", err);
            return null;
        }
    }

    public static Domain toIDMDomain(
            org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain domain) {
        try {
            return (Domain) fromMDSALObject(domain, Domain.class);
        } catch (Exception err) {
            LOG.error("Error converting domain from MDSAL to IDM object", err);
            return null;
        }
    }

    // Convert Role
    public static org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role toMDSALRole(
            Role role) {
        try {
            return (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role) toMDSALObject(
                    role, RoleBuilder.class);
        } catch (Exception err) {
            LOG.error("Error converting role to MDSAL object", err);
            return null;
        }
    }

    public static Role toIDMRole(
            org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role role) {
        try {
            return (Role) fromMDSALObject(role, Role.class);
        } catch (Exception err) {
            LOG.error("Error converting role fom MDSAL to IDM object", err);
            return null;
        }
    }

    // Convert User
    public static org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User toMDSALUser(
            User user) {
        try {
            return (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User) toMDSALObject(
                    user, UserBuilder.class);
        } catch (Exception err) {
            LOG.error("Error converting user to MDSAL object", err);
            return null;
        }
    }

    public static User toIDMUser(
            org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User user) {
        try {
            return (User) fromMDSALObject(user, User.class);
        } catch (Exception err) {
            LOG.error("Error converting user from MDSAL to IDM object", err);
            return null;
        }
    }

    // Convert Grant
    public static org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant toMDSALGrant(
            Grant grant) {
        try {
            return (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant) toMDSALObject(
                    grant, GrantBuilder.class);
        } catch (Exception err) {
            LOG.error("Error converting grant to MDSAL object", err);
            return null;
        }
    }

    public static Grant toIDMGrant(
            org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant grant) {
        try {
            return (Grant) fromMDSALObject(grant, Grant.class);
        } catch (Exception err) {
            LOG.error("Error converting grant from MDSAL to IDM object", err);
            return null;
        }
    }
}
