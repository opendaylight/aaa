/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.common.model;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;

/**
 * User filter.
 * 
 * @author Fabiel Zuniga
 */
public class UserFilter {

    /*
     * The filter is the contract of supported queries (Supported conditions and their combination).
     * Visitor pattern is used to get the actual filter.
     */
    private Filter filter;

    private UserFilter() {
    }

    /**
     * Creates a neutral filter that returns all network devices
     * 
     * @return a neutral filter
     */
    public static UserFilter filterAll() {
        UserFilter filter = new UserFilter();
        filter.filter = new All();
        return filter;
    }

    /**
     * Creates a filter to retrieve users by enabled status
     * 
     * @param enabled enabled status
     * @return a filter
     */
    public static UserFilter filterByEnabledStatus(boolean enabled) {
        UserFilter filter = new UserFilter();
        filter.filter = new ByEnabledStatus(enabled);
        return filter;
    }

    /**
     * Accepts a visitor.
     * 
     * @param visitor visitor
     * @return the result of the visit
     */
    public <T> T accept(Visitor<T> visitor) {
        return this.filter.accept(visitor);
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("filter", this.filter)
        );
    }

    private static abstract class Filter {
        protected EqualityCondition<Username> usernameCondition;
        protected EqualityCondition<Password> passwordCondition;
        protected EqualityCondition<Boolean> enabledCondition;

        protected abstract <T> T accept(Visitor<T> visitor);

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("usernameCondition", this.usernameCondition),
                    Property.valueOf("passwordCondition", this.passwordCondition),
                    Property.valueOf("enabledCondition", this.enabledCondition)
            );
        }
    }

    /**
     * Neutral filter that returns all users.
     */
    public static class All extends Filter {

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter that retrieves users by enabled status.
     */
    public static class ByEnabledStatus extends Filter {

        /**
         * Creates a filter
         * 
         * @param enabled enabling status
         */
        public ByEnabledStatus(boolean enabled) {
            this.enabledCondition = EqualityCondition.equalTo(Boolean.valueOf(enabled));
        }

        /**
         * Gets the enabled status condition.
         * 
         * @return the condition
         */
        public EqualityCondition<Boolean> getEnabledStatusCondition() {
            return this.enabledCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter visitor.
     * 
     * @param <T> type of the result of the visit
     */
    public static interface Visitor<T> {

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(All filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByEnabledStatus filter);
    }
}
