/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import java.io.Serializable;

import com.hp.util.common.Converter;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerializableValueType;

/**
 * {@link Converter} from {@link Id} to its value.
 * 
 * @param <T> type of the object with id
 * @param <I> type of the id value
 * @author Fabiel Zuniga
 */
public class IdValueConverter<T, I extends Serializable> implements Converter<Id<T, I>, I> {

    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    @SuppressWarnings("rawtypes")
    private static final IdValueConverter INSTANCE = new IdValueConverter();

    private IdValueConverter() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    @SuppressWarnings("unchecked")
    public static <T, I extends Serializable> Converter<Id<T, I>, I> getInstance() {
        return INSTANCE;
    }

    @Override
    public I convert(Id<T, I> source) {
        return SerializableValueType.toValue(source);
    }
}
