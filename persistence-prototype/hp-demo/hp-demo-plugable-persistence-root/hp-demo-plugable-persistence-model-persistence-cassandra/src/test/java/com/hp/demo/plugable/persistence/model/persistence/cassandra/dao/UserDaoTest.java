/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.cassandra.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.UserDaoTest.AstyanaxUserDao;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDaoTest;
import com.hp.util.model.persistence.dao.SearchCase;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class UserDaoTest extends CassandraMarkPageDaoTest<Username, User, UserFilter, Void, AstyanaxUserDao> {

    @Override
    protected AstyanaxUserDao createDaoInstance() {
        return new AstyanaxUserDao();
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected User createIdentifiable(Id<User, Username> id) {
        User user = new User(id);
        user.setPassword(Password.valueOf("Password " + id.getValue().getValue()));
        user.setEmail(Email.valueOf(id.getValue().getValue() + "@user.com"));
        user.setEnabled(true);
        user.setDescription("Description " + id.getValue().getValue());

        return user;
    }

    @Override
    protected List<User> createIdentifiables(int count) {
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < count; i++) {
            Username username = Username.valueOf("user-" + i);
            Id<User, Username> id = Id.valueOf(username);
            users.add(createIdentifiable(id));
        }
        return users;
    }

    @Override
    protected void modify(User user) {
        user.setPassword(null);
        user.setEmail(Email.valueOf("other-email@user.com"));
        user.setEnabled(false);
        user.setDescription("Other description");
    }

    @Override
    protected void assertEqualState(User expected, User actual) {
        Assert.assertEquals(expected.getPassword(), actual.getPassword());
        Assert.assertEquals(expected.getEmail(), actual.getEmail());
        Assert.assertEquals(expected.isEnabled(), actual.isEnabled());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
    }

    @Override
    protected List<SearchCase<User, UserFilter, Void>> getSearchCases() {
        List<SearchCase<User, UserFilter, Void>> searchCases = new ArrayList<SearchCase<User, UserFilter, Void>>();

        Username username1 = Username.valueOf("username-1");
        Username username2 = Username.valueOf("username-2");
        Username username3 = Username.valueOf("username-3");
        Username username4 = Username.valueOf("username-4");
        Username username5 = Username.valueOf("username-5");

        Id<User, Username> id1 = Id.valueOf(username1);
        Id<User, Username> id2 = Id.valueOf(username2);
        Id<User, Username> id3 = Id.valueOf(username3);
        Id<User, Username> id4 = Id.valueOf(username4);
        Id<User, Username> id5 = Id.valueOf(username5);

        Password password1 = Password.valueOf("password-1");
        Password password2 = Password.valueOf("password-2");
        Password password3 = null;
        Password password4 = Password.valueOf("password-4");
        Password password5 = Password.valueOf("password-5");

        User dto1 = new User(id1);
        User dto2 = new User(id2);
        User dto3 = new User(id3);
        User dto4 = new User(id4);
        User dto5 = new User(id5);

        dto1.setPassword(password1);
        dto2.setPassword(password2);
        dto3.setPassword(password3);
        dto4.setPassword(password4);
        dto5.setPassword(password5);

        dto1.setEnabled(true);
        dto2.setEnabled(true);
        dto3.setEnabled(false);
        dto4.setEnabled(true);
        dto5.setEnabled(false);

        List<User> searchSpace = new ArrayList<User>(5);
        searchSpace.add(dto1);
        searchSpace.add(dto2);
        searchSpace.add(dto3);
        searchSpace.add(dto4);
        searchSpace.add(dto5);

        // SQL systems normally use primary key as the default sorting

        SortSpecification<Void> sortSpecification = null;

        //

        UserFilter filter = UserFilter.filterByEnabledStatus(true);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto1, dto2, dto4));

        //

        filter = UserFilter.filterByEnabledStatus(false);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto3, dto5));

        //

        return searchCases;
    }

    public static class AstyanaxUserDao extends UserDao<Astyanax> {
        /*
         * Class to allow using Astyanax-based integration test.
         */
    }
}
