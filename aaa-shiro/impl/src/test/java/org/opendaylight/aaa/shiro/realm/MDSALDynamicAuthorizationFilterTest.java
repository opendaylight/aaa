/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFluentFuture;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Tests the Dynamic Authorization Filter.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@ExtendWith(MockitoExtension.class)
class MDSALDynamicAuthorizationFilterTest {
    private static final Set<Permissions.Actions> ACTIONS_SET = Set.of(Permissions.Actions.Get, Permissions.Actions.Put,
        Permissions.Actions.Delete, Permissions.Actions.Patch, Permissions.Actions.Post);

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpAuthorization httpAuthorization;
    @Mock
    private Policies policies;
    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies
        .Policies innerPolicies;
    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies
        .Policies innerPolicies2;
    @Mock
    private Subject subject;
    @Mock
    private Permissions permissions;
    @Mock
    private Permissions permissions2;

    @Test
    void testBasicAccessWithNoRules() throws Exception {
        when(request.getRequestURI()).thenReturn("abc");

        //
        // Test Setup: No rules are added to the HttpAuthorization container.  Open access should be allowed.
        var filter = newFilter(subject, mockDataBroker(null));

        // Ensure that access is allowed since no data is returned from the MDSAL read.
        // This is through making sure the Optional is not present.
        assertTrue(filter.isAccessAllowed(request, null, null));

        //
        // Same as above, but with an empty policy list returned.
        when(policies.nonnullPolicies()).thenReturn(List.of());
        when(httpAuthorization.nonnullPolicies()).thenReturn(policies);
        filter = newFilter(subject, mockDataBroker(httpAuthorization));

        assertTrue(filter.isAccessAllowed(request, null, null));
    }

    @Test
    void testMDSALExceptionDuringRead() throws Exception {
        // Test Setup: No rules are added to the HttpAuthorization container.  The MDSAL read
        // is instructed to return an immediateFailedFluentFuture, to emulate an error in reading
        // the Data Store.
        when(request.getRequestURI()).thenReturn("abc");

        final var filter = newFilter(subject, mockDataBroker(new ReadFailedException("Test Fail")));

        // Ensure that if an error occurs while reading MD-SAL that access is denied.
        assertFalse(filter.isAccessAllowed(request, null, null));
    }

    @Test
    void testBasicAccessWithOneRule() throws Exception {

        //
        // Test Setup:
        //
        // A Rule is added to match /** allowing HTTP PUT for the admin role.
        // All other Methods are considered unauthorized.

        when(permissions.getRole()).thenReturn("admin");
        when(permissions.getActions()).thenReturn(Set.of(Permissions.Actions.Put));
        when(innerPolicies.getResource()).thenReturn("/**");
        when(innerPolicies.nonnullPermissions()).thenReturn(List.of(permissions));
        when(policies.nonnullPolicies()).thenReturn(List.of(innerPolicies));
        when(httpAuthorization.nonnullPolicies()).thenReturn(policies);

        final var filter = newFilter(subject, mockDataBroker(httpAuthorization));
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
    void testSeveralMatchingRules() throws Exception {
        //
        // Test Setup:
        //
        // Create some mock data which has a couple of rules which may/may not match.  This
        // test ensures the correct application of said rules.
        final var role = "admin";
        final var resource = "/**";
        final var resource2 = "/specialendpoint/**";
        when(permissions.getRole()).thenReturn(role);
        when(permissions.getActions()).thenReturn(ACTIONS_SET);
        when(innerPolicies.getResource()).thenReturn(resource);
        when(innerPolicies.getIndex()).thenReturn(Uint32.valueOf(5));
        when(innerPolicies.nonnullPermissions()).thenReturn(List.of(permissions));
        when(innerPolicies2.getResource()).thenReturn(resource2);
        when(innerPolicies2.getIndex()).thenReturn(Uint32.TEN);
        when(permissions2.getRole()).thenReturn("dog");
        when(permissions2.getActions()).thenReturn(ACTIONS_SET);
        when(innerPolicies2.nonnullPermissions()).thenReturn(List.of(permissions2));
        when(policies.nonnullPolicies()).thenReturn(List.of(innerPolicies, innerPolicies2));
        when(httpAuthorization.nonnullPolicies()).thenReturn(policies);

        final var filter = newFilter(subject, mockDataBroker(httpAuthorization));
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
        when(policies.nonnullPolicies()).thenReturn(List.of(innerPolicies2, innerPolicies));
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
    void testMultiplePolicies() throws Exception {
        // admin can do anything
        final var role = "admin";
        final var resource = "/**";
        when(permissions.getRole()).thenReturn(role);
        when(permissions.getActions()).thenReturn(ACTIONS_SET);
        when(permissions2.getRole()).thenReturn("user");
        when(permissions2.getActions()).thenReturn(Set.of(Permissions.Actions.Get));
        when(innerPolicies.getResource()).thenReturn(resource);
        when(innerPolicies.nonnullPermissions()).thenReturn(List.of(permissions, permissions2));
        when(policies.nonnullPolicies()).thenReturn(List.of(innerPolicies));
        when(httpAuthorization.nonnullPolicies()).thenReturn(policies);

        final var filter = newFilter(subject, mockDataBroker(httpAuthorization));
        when(request.getRequestURI()).thenReturn("/abc");
        when(request.getMethod()).thenReturn("Put");
        when(subject.hasRole("admin")).thenReturn(false);
        when(subject.hasRole("user")).thenReturn(true);

        assertFalse(filter.isAccessAllowed(request, null, null));
        when(request.getMethod()).thenReturn("Get");
        assertTrue(filter.isAccessAllowed(request, null, null));
    }

    private static DataBroker mockDataBroker(final Object readData) {
        final var readOnlyTransaction = mock(ReadTransaction.class);
        if (readData instanceof DataObject dataObject) {
            doReturn(immediateFluentFuture(Optional.of(dataObject)))
                .when(readOnlyTransaction).read(any(), any(InstanceIdentifier.class));
        } else if (readData instanceof Exception cause) {
            doReturn(immediateFailedFluentFuture(cause)).when(readOnlyTransaction)
                .read(any(), any(InstanceIdentifier.class));
        } else {
            doReturn(immediateFluentFuture(Optional.empty())).when(readOnlyTransaction)
                .read(any(), any(InstanceIdentifier.class));
        }

        final var mockDataBroker = mock(DataBroker.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        return mockDataBroker;
    }

    private static MDSALDynamicAuthorizationFilter newFilter(final Subject subject, final DataBroker dataBroker)
            throws ServletException {
        final var ret = new MDSALDynamicAuthorizationFilter(dataBroker) {
            @Override
            protected Subject getSubject(final ServletRequest servletRequest, final ServletResponse servletResponse) {
                return subject;
            }
        };

        ret.processPathConfig("test-path","test-config");
        return ret;
    }
}
