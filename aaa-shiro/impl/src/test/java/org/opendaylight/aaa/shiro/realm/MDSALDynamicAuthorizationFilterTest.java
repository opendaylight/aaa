/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.shiro.web.env.ThreadLocals;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Tests the Dynamic Authorization Filter.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class MDSALDynamicAuthorizationFilterTest {

    @Before
    public void setup() {
    }

    private static DataBroker mockDataBroker(final Object readData) {
        final ReadTransaction readOnlyTransaction = mock(ReadTransaction.class);

        if (readData instanceof DataObject) {
            doReturn(immediateFluentFuture(Optional.of((DataObject)readData)))
                    .when(readOnlyTransaction).read(any(), any());
        } else if (readData instanceof Exception) {
            doReturn(immediateFailedFluentFuture((Exception)readData)).when(readOnlyTransaction).read(any(), any());
        } else {
            doReturn(immediateFluentFuture(Optional.empty())).when(readOnlyTransaction).read(any(), any());
        }

        final DataBroker mockDataBroker = mock(DataBroker.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        return mockDataBroker;
    }

    private static MDSALDynamicAuthorizationFilter newFilter(final Subject subject, final DataBroker dataBroker)
            throws ServletException {
        ThreadLocals.DATABROKER_TL.set(dataBroker);
        MDSALDynamicAuthorizationFilter ret;
        try {
            ret = new MDSALDynamicAuthorizationFilter() {
                @Override
                protected Subject getSubject(final ServletRequest request, final ServletResponse servletResponse) {
                    return subject;
                }
            };
        } finally {
            ThreadLocals.DATABROKER_TL.remove();
        }

        ret.processPathConfig("test-path","test-config");
        return ret;
    }

    // test helper method to generate some cool mdsal data
    private static DataBroker getTestData() throws Exception {
        return getTestData("/**", "admin", "Default Test AuthZ Rule", Permissions.Actions.Put);
    }

    // test helper method to generate some cool mdsal data
    private static DataBroker getTestData(final String resource, final String role, final String description,
                                   final Permissions.Actions actions) throws Exception {

        final List<Permissions.Actions> actionsList = Lists.newArrayList(actions);
        final Permissions permissions = mock(Permissions.class);
        when(permissions.getRole()).thenReturn(role);
        when(permissions.getActions()).thenReturn(actionsList);
        final List<Permissions> permissionsList = Lists.newArrayList(permissions);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies
                innerPolicies = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                        .policies.Policies.class);
        when(innerPolicies.getResource()).thenReturn(resource);
        when(innerPolicies.getDescription()).thenReturn(description);
        when(innerPolicies.getPermissions()).thenReturn(permissionsList);
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies>
                policiesList = Lists.newArrayList(innerPolicies);
        final Policies policies = mock(Policies.class);
        when(policies.getPolicies()).thenReturn(policiesList);
        final HttpAuthorization httpAuthorization = mock(HttpAuthorization.class);
        when(httpAuthorization.getPolicies()).thenReturn(policies);

        return mockDataBroker(httpAuthorization);
    }

    @Test
    public void testBasicAccessWithNoRules() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("abc");
        when(request.getMethod()).thenReturn("Put");

        //
        // Test Setup: No rules are added to the HttpAuthorization container.  Open access should be allowed.
        MDSALDynamicAuthorizationFilter filter = newFilter(mock(Subject.class), mockDataBroker(null));

        // Ensure that access is allowed since no data is returned from the MDSAL read.
        // This is through making sure the Optional is not present.
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Same as above, but with an empty policy list returned.

        final Policies policies = mock(Policies.class);
        when(policies.getPolicies()).thenReturn(new ArrayList<>());
        final HttpAuthorization httpAuthorization = mock(HttpAuthorization.class);
        when(httpAuthorization.getPolicies()).thenReturn(policies);
        filter = newFilter(mock(Subject.class), mockDataBroker(httpAuthorization));

        assertTrue(filter.isAccessAllowed(request, null, null));
    }

    @Test
    public void testMDSALExceptionDuringRead() throws Exception {
        // Test Setup: No rules are added to the HttpAuthorization container.  The MDSAL read
        // is instructed to return an immediateFailedFluentFuture, to emulate an error in reading
        // the Data Store.

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("abc");
        when(request.getMethod()).thenReturn("Put");

        MDSALDynamicAuthorizationFilter filter = newFilter(mock(Subject.class),
                mockDataBroker(new ReadFailedException("Test Fail")));

        // Ensure that if an error occurs while reading MD-SAL that access is denied.
        assertFalse(filter.isAccessAllowed(request, null, null));
    }

    @Test
    public void testBasicAccessWithOneRule() throws Exception {

        //
        // Test Setup:
        //
        // A Rule is added to match /** allowing HTTP PUT for the admin role.
        // All other Methods are considered unauthorized.

        final Subject subject = mock(Subject.class);
        final MDSALDynamicAuthorizationFilter filter = newFilter(subject, getTestData());

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("abc");
        when(request.getMethod()).thenReturn("Put");
        when(subject.hasRole("admin")).thenReturn(true);

        //
        // Test Case 1:
        //
        // Make a PUT HTTP request from a Subject with the admin role.  The request URL does not match,
        // since "abc" does not start with a "/" character.  Since no rule exists for this particular request,
        // then access should be allowed.
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 2:
        //
        // Repeat of the above against a matching endpoint.  Access should be allowed.
        when(request.getRequestURI()).thenReturn("/anotherexamplethatshouldwork");
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 3:
        //
        // Repeat of the above request against a more complex endpoint.  Access should be allowed.
        when(request.getRequestURI()).thenReturn("/auth/v1/users");
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 4:
        //
        // Negative test case-- ensure that when an unallowed method (POST) is tried with an otherwise
        // allowable request, that access is denied.
        when(request.getMethod()).thenReturn("Post");
        assertFalse(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 5:
        //
        // Negative test case-- ensure that when an unallowed role is tried with an otherwise allowable
        // request, that acess is denied.
        when(request.getMethod()).thenReturn("Put");
        when(subject.hasRole("admin")).thenReturn(false);
        assertFalse(filter.isAccessAllowed(request, null, null));
    }

    @Test
    public void testSeveralMatchingRules() throws Exception {
        //
        // Test Setup:
        //
        // Create some mock data which has a couple of rules which may/may not match.  This
        // test ensures the correct application of said rules.
        final List<Permissions.Actions> actionsList = Lists
                .newArrayList(Permissions.Actions.Get, Permissions.Actions.Delete, Permissions.Actions.Patch,
                              Permissions.Actions.Put, Permissions.Actions.Post);
        final String role = "admin";
        final String resource = "/**";
        final String resource2 = "/specialendpoint/**";
        final String description = "All encompassing rule";
        final Permissions permissions = mock(Permissions.class);
        when(permissions.getRole()).thenReturn(role);
        when(permissions.getActions()).thenReturn(actionsList);
        final List<Permissions> permissionsList = Lists.newArrayList(permissions);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies
                innerPolicies = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                        .policies.Policies.class);
        when(innerPolicies.getResource()).thenReturn(resource);
        when(innerPolicies.getIndex()).thenReturn(Uint32.valueOf(5));
        when(innerPolicies.getDescription()).thenReturn(description);
        when(innerPolicies.getPermissions()).thenReturn(permissionsList);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies
                innerPolicies2 = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                        .policies.Policies.class);
        when(innerPolicies2.getResource()).thenReturn(resource2);
        when(innerPolicies2.getIndex()).thenReturn(Uint32.valueOf(10));
        final Permissions permissions2 = mock(Permissions.class);
        when(permissions2.getRole()).thenReturn("dog");
        when(permissions2.getActions()).thenReturn(actionsList);
        when(innerPolicies2.getPermissions()).thenReturn(Lists.newArrayList(permissions2));
        when(innerPolicies2.getDescription()).thenReturn("Specialized Rule");
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies>
                policiesList = Lists.newArrayList(innerPolicies, innerPolicies2);
        final Policies policies = mock(Policies.class);
        when(policies.getPolicies()).thenReturn(policiesList);
        final HttpAuthorization httpAuthorization = mock(HttpAuthorization.class);
        when(httpAuthorization.getPolicies()).thenReturn(policies);

        final Subject subject = mock(Subject.class);
        final MDSALDynamicAuthorizationFilter filter = newFilter(subject, mockDataBroker(httpAuthorization));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/abc");
        when(request.getMethod()).thenReturn("Put");
        when(subject.hasRole("admin")).thenReturn(true);

        //
        // Test Case 1:
        //
        // In the setup, two rules were added.  First, make sure that the first rule is working.
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 2:
        //
        // Both rules would technically match the input request URI.  We want to make sure that
        // order is respected.  We do this by ensuring access is granted (i.e., the first rule is matched).
        when(request.getRequestURI()).thenReturn("/specialendpoint");
        assertTrue(filter.isAccessAllowed(request, null, null));
        when(request.getRequestURI()).thenReturn("/specialendpoint/");
        assertTrue(filter.isAccessAllowed(request, null, null));
        when(request.getRequestURI()).thenReturn("/specialendpoint/somewhatextended");
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Test Case 3:
        //
        // Now reverse the ordering of the rules, and ensure that access is denied (except for
        // the first non-applicable rule, which should still be allowed).  This is
        // because the Subject making the request is not granted the "dog" role.
        policiesList = Lists.newArrayList(innerPolicies2, innerPolicies);
        when(policies.getPolicies()).thenReturn(policiesList);
        // Modify Index to ensure the innerPolicies2 actually gets
        // used instead of innerPolicies
        when(innerPolicies2.getIndex()).thenReturn(Uint32.valueOf(4));
        when(request.getRequestURI()).thenReturn("/abc");
        assertTrue(filter.isAccessAllowed(request, null, null));
        when(request.getRequestURI()).thenReturn("/specialendpoint");
        assertFalse(filter.isAccessAllowed(request, null, null));
        when(request.getRequestURI()).thenReturn("/specialendpoint/");
        assertFalse(filter.isAccessAllowed(request, null, null));
        when(request.getRequestURI()).thenReturn("/specialendpoint/somewhatextended");
        assertFalse(filter.isAccessAllowed(request, null, null));
    }

    @Test
    public void testMultiplePolicies() throws Exception {
        // admin can do anything
        final String role = "admin";
        final String resource = "/**";
        final String description = "Test description";
        final List<Permissions.Actions> actionsList = Lists
                .newArrayList(Permissions.Actions.Get, Permissions.Actions.Put, Permissions.Actions.Delete,
                              Permissions.Actions.Patch, Permissions.Actions.Post);
        final Permissions permissions = mock(Permissions.class);
        when(permissions.getRole()).thenReturn(role);
        when(permissions.getActions()).thenReturn(actionsList);
        final Permissions permissions2 = mock(Permissions.class);
        when(permissions2.getRole()).thenReturn("user");
        when(permissions2.getActions()).thenReturn(Lists.newArrayList(Permissions.Actions.Get));
        final List<Permissions> permissionsList = Lists.newArrayList(permissions, permissions2);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies
                innerPolicies = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                        .policies.Policies.class);
        when(innerPolicies.getResource()).thenReturn(resource);
        when(innerPolicies.getDescription()).thenReturn(description);
        when(innerPolicies.getPermissions()).thenReturn(permissionsList);
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization
                .policies.Policies>
                policiesList = Lists.newArrayList(innerPolicies);
        final Policies policies = mock(Policies.class);
        when(policies.getPolicies()).thenReturn(policiesList);
        final HttpAuthorization httpAuthorization = mock(HttpAuthorization.class);
        when(httpAuthorization.getPolicies()).thenReturn(policies);

        final Subject subject = mock(Subject.class);
        final MDSALDynamicAuthorizationFilter filter = newFilter(subject, mockDataBroker(httpAuthorization));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/abc");
        when(request.getMethod()).thenReturn("Put");
        when(subject.hasRole("admin")).thenReturn(false);
        when(subject.hasRole("user")).thenReturn(true);

        assertFalse(filter.isAccessAllowed(request, null, null));
        when(request.getMethod()).thenReturn("Get");
        assertTrue(filter.isAccessAllowed(request, null, null));

    }
}
