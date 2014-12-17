/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.jpa.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.All;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByLocation;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByLocationAndReachabilityStatus;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByReachabilityStatus;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.model.persistence.jpa.entity.NetworkDeviceEntity;
import com.hp.demo.plugable.persistence.model.persistence.jpa.entity.NetworkDeviceEntity_;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.model.persistence.jpa.dao.JpaMappedKeyDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;

/**
 * Network device DAO.
 * 
 * @author Fabiel Zuniga
 */
public class NetworkDeviceDao
        extends
        JpaMappedKeyDao<SerialNumber, NetworkDevice, String, NetworkDeviceEntity, NetworkDeviceFilter, NetworkDeviceSortKey> {

    /**
     * Creates a DAO.
     */
    public NetworkDeviceDao() {
        super(NetworkDeviceEntity.class);
    }

    @Override
    protected SerialNumber getId(NetworkDeviceEntity entity) {
        return entity.getId();
    }

    @Override
    protected String mapKey(SerialNumber key) {
        return key.getValue();
    }

    @Override
    protected NetworkDeviceEntity create(NetworkDevice device) {
        NetworkDeviceEntity entity = new NetworkDeviceEntity(device.getId().getValue(), device.getMacAddress(),
                device.getReachabilityStatus());
        entity.setIpAddress(device.getIpAddress());
        entity.setFriendlyName(device.getFriendlyName());
        entity.setLocation(device.getLocation());
        return entity;
    }

    @Override
    protected NetworkDevice doConvert(NetworkDeviceEntity source) {
        Id<NetworkDevice, SerialNumber> id = Id.valueOf(source.getId());
        NetworkDevice device = new NetworkDevice(id, source.getMacAddress(), source.getReachabilityStatus());
        device.setIpAddress(source.getIpAddress());
        device.setFriendlyName(source.getFriendlyName());
        device.setLocation(source.getLocation());
        return device;
    }

    @Override
    protected void conform(NetworkDeviceEntity target, NetworkDevice source) {
        target.setIpAddress(source.getIpAddress());
        target.setFriendlyName(source.getFriendlyName());
        target.setLocation(source.getLocation());
        target.setReachabilityStatus(source.getReachabilityStatus());
    }

    @Override
    protected Predicate getQueryPredicate(NetworkDeviceFilter networkDeviceFilter, final CriteriaBuilder builder,
            final Root<NetworkDeviceEntity> root) {
        NetworkDeviceFilter.Visitor<Predicate> visitor = new NetworkDeviceFilter.Visitor<Predicate>() {

            @Override
            public Predicate visit(All filter) {
                return null;
            }

            @Override
            public Predicate visit(ByLocation filter) {
                return getQueryPredicateGenerator().getPredicate(filter.getLocationCondition(),
                        NetworkDeviceEntity_.location, builder, root);
            }

            @Override
            public Predicate visit(ByReachabilityStatus filter) {
                return getQueryPredicateGenerator().getPredicate(filter.getReachabilityStatusCondition(),
                        NetworkDeviceEntity_.reachabilityStatus, builder, root);
            }

            @Override
            public Predicate visit(ByLocationAndReachabilityStatus filter) {
                JpaQueryPredicateGenerator<NetworkDeviceEntity> predicateGenerator = getQueryPredicateGenerator();
                Predicate locationPredicate = predicateGenerator.getPredicate(filter.getLocationCondition(),
                        NetworkDeviceEntity_.location, builder, root);
                Predicate reachabilityStatusPredicate = predicateGenerator
                        .getPredicate(filter.getReachabilityStatusCondition(), NetworkDeviceEntity_.reachabilityStatus,
                                builder, root);
                return predicateGenerator.and(builder, locationPredicate, reachabilityStatusPredicate);
            }
        };

        return nonnull(networkDeviceFilter).accept(visitor);
    }

    @Override
    protected SingularAttribute<? super NetworkDeviceEntity, ?> getSingularAttribute(NetworkDeviceSortKey sortKey) {
        switch (sortKey) {
            case FRIENDLY_NAME:
                return NetworkDeviceEntity_.friendlyName;
        }
        return null;
    }

    private static NetworkDeviceFilter nonnull(NetworkDeviceFilter filter) {
        if (filter != null) {
            return filter;
        }
        return NetworkDeviceFilter.filterAll();
    }
}
