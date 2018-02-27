/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import static com.google.common.collect.Iterators.asEnumeration;

import com.google.common.collect.ImmutableMap;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Bridge {@link Dictionary} to {@link Map}.
 *
 * <p>
 * Old OSGi APIs use {@link Dictionary}, but the only concrete implementation of
 * Dictionary in the JDK is Hashtable, which both error-prone as well as
 * Modernizer don't like and suggest HashMap - but that is not a Dictionary.
 *
 * @author Michael Vorburger.ch
 */
@NotThreadSafe
class MapDictionary<K, V> extends Dictionary<K, V> {

    private final Map<K, V> map;

    MapDictionary(Map<K, V> map) {
        this.map = ImmutableMap.copyOf(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Enumeration<K> keys() {
        return asEnumeration(map.keySet().iterator());
    }

    @Override
    public Enumeration<V> elements() {
        return asEnumeration(map.values().iterator());
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

}
