/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate.search;

import org.opendaylight.aaa.persistence.api.Page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchPage<T, ID extends Serializable> implements Page<T,ID>{

    ID previousMarker;
    List<T> entries = new ArrayList<T>();
    ID nextMarker;


    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Iterator<T> content() {
        return entries.iterator();
    }

    @Override
    public boolean hasNext() {
        return (nextMarker != null);
    }

    @Override
    public ID nextMarker() {
        return nextMarker;
    }

    @Override
    public boolean hasPrevious() {
        return (previousMarker != null);
    }

    @Override
    public ID previousMarker() {
        return previousMarker;
    }

    public void setContent(List<T> content) {
        this.entries = content;
    }

    public void setPreviousMarker(ID id) {
        this.previousMarker = id;
    }

    public void setNextMarker(ID id) {
        this.nextMarker = id;
    }
}
