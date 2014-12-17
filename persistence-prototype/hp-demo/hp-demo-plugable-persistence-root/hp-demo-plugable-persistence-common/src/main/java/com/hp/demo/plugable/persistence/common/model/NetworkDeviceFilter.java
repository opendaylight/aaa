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
import com.hp.util.common.type.net.ReachabilityStatus;

/**
 * Network device filter.
 * 
 * @author Fabiel Zuniga
 */
public class NetworkDeviceFilter {

    /*
     * The filter is the contract of supported queries (Supported conditions and their combination).
     * Visitor pattern is used to get the actual filter.
     */
    private Filter filter;

    private NetworkDeviceFilter() {
    }

    /**
     * Creates a neutral filter that returns all network devices
     * 
     * @return a neutral filter
     */
    public static NetworkDeviceFilter filterAll() {
        NetworkDeviceFilter filter = new NetworkDeviceFilter();
        filter.filter = new All();
        return filter;
    }

    /**
     * Creates a filter to retrieve network devices by location
     * 
     * @param location location
     * @return a filter
     */
    public static NetworkDeviceFilter filterByLocation(Location location) {
        NetworkDeviceFilter filter = new NetworkDeviceFilter();
        filter.filter = new ByLocation(location);
        return filter;
    }

    /**
     * Creates a filter to retrieve network devices by reachability status
     * 
     * @param reachabilityStatus reachability status
     * @return a filter
     */
    public static NetworkDeviceFilter filterByReachabilityStatus(ReachabilityStatus reachabilityStatus) {
        NetworkDeviceFilter filter = new NetworkDeviceFilter();
        filter.filter = new ByReachabilityStatus(reachabilityStatus);
        return filter;
    }

    /**
     * Creates a filter to retrieve network devices by location and reachability status
     * 
     * @param location location
     * @param reachabilityStatus reachability status
     * @return a filter
     */
    public static NetworkDeviceFilter filterByLocationAndByReachabilityStatus(Location location,
            ReachabilityStatus reachabilityStatus) {
        NetworkDeviceFilter filter = new NetworkDeviceFilter();
        filter.filter = new ByLocationAndReachabilityStatus(location, reachabilityStatus);
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
        return ObjectToStringConverter.toString(this,
                Property.valueOf("filter", this.filter)
        );
    }

    private static abstract class Filter {
        protected EqualityCondition<Location> locationCondition;
        protected EqualityCondition<ReachabilityStatus> reachabilityStatusCondition;

        protected abstract <T> T accept(Visitor<T> visitor);
        
        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("locationCondition", this.locationCondition),
                    Property.valueOf("reachabilityStatusCondition", this.reachabilityStatusCondition)
            );
        }
    }

    /**
     * Neutral filter that returns all network devices.
     */
    public static class All extends Filter {

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter that retrieves network devices by location.
     */
    public static class ByLocation extends Filter {

        /**
         * Creates a filter.
         * 
         * @param location location
         */
        public ByLocation(Location location) {
            if (location == null) {
                throw new NullPointerException("location cannot be null");
            }

            this.locationCondition = EqualityCondition.equalTo(location);
        }

        /**
         * Gets the location condition.
         * 
         * @return the condition
         */
        public EqualityCondition<Location> getLocationCondition() {
            return this.locationCondition;
        }
        
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter that retrieves network devices by reachability status.
     */
    public static class ByReachabilityStatus extends Filter {

        /**
         * Creates a filter
         * 
         * @param reachabilityStatus reachability status
         */
        public ByReachabilityStatus(ReachabilityStatus reachabilityStatus) {
            if (reachabilityStatus == null) {
                throw new NullPointerException("reachabilityStatus cannot be null");
            }

            this.reachabilityStatusCondition = EqualityCondition.equalTo(reachabilityStatus);
        }

        /**
         * Gets the reachability status condition.
         * 
         * @return the condition
         */
        public EqualityCondition<ReachabilityStatus> getReachabilityStatusCondition() {
            return this.reachabilityStatusCondition;
        }
        
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter that retrieves network devices by location and reachability status.
     */
    public static class ByLocationAndReachabilityStatus extends Filter {

        /**
         * Creates a filter.
         * 
         * @param location location
         * @param reachabilityStatus reachability status
         */
        public ByLocationAndReachabilityStatus(Location location, ReachabilityStatus reachabilityStatus) {
            if (location == null) {
                throw new NullPointerException("location cannot be null");
            }

            if (reachabilityStatus == null) {
                throw new NullPointerException("reachabilityStatus cannot be null");
            }

            this.locationCondition = EqualityCondition.equalTo(location);
            this.reachabilityStatusCondition = EqualityCondition.equalTo(reachabilityStatus);
        }

        /**
         * Gets the location condition.
         * 
         * @return the condition
         */
        public EqualityCondition<Location> getLocationCondition() {
            return this.locationCondition;
        }

        /**
         * Gets the reachability status condition.
         * 
         * @return the condition
         */
        public EqualityCondition<ReachabilityStatus> getReachabilityStatusCondition() {
            return this.reachabilityStatusCondition;
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
        public T visit(ByLocation filter);
        
        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByReachabilityStatus filter);
        
        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByLocationAndReachabilityStatus filter);
    }
}
