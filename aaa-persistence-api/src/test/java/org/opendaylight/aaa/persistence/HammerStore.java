/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence;

import org.opendaylight.aaa.persistence.api.Criteria;
import org.opendaylight.aaa.persistence.api.ObjectStore;
import org.opendaylight.aaa.persistence.api.Page;
import org.opendaylight.aaa.persistence.api.Pageable;
import org.opendaylight.aaa.persistence.api.Restrictable;
import org.opendaylight.aaa.persistence.api.Restriction;

public class HammerStore implements ObjectStore<Hammer, Long> {

    @Override
    public <S extends Hammer> S save(S object) {
        return object;
    }

    @Override
    public <S extends Hammer> Iterable<S> save(Iterable<S> objects) {
        return objects;
    }

    @Override
    public Hammer findById(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Hammer> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page<Hammer, Long> findAll(Pageable<Long> p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Hammer> findAll(Hammer example) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<Hammer> findAll(Hammer example, Criteria c) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long count() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long count(Hammer example) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long count(Hammer example, Restrictable r) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(Hammer object) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Long deleteAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long deleteAll(Hammer example) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long deleteAll(Hammer example, Restriction r) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Long deleteAll(Iterable<? extends Hammer> objects) {
        // TODO Auto-generated method stub
        return null;
    }

}
