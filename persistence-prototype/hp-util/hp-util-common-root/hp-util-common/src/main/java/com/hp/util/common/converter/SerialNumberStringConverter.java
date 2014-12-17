/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SerializableValueType;

/**
 * {@link BidirectionalConverter} from {@link SerialNumber} to {@link String}.
 * 
 * @author Fabiel Zuniga
 */
public final class SerialNumberStringConverter implements BidirectionalConverter<SerialNumber, String> {
    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    private static final BidirectionalConverter<SerialNumber, String> INSTANCE = new SerialNumberStringConverter();

    private SerialNumberStringConverter() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    public static BidirectionalConverter<SerialNumber, String> getInstance() {
        return INSTANCE;
    }

    @Override
    public String convert(SerialNumber source) {
        return SerializableValueType.toValue(source);
    }

    @Override
    public SerialNumber restore(String target) throws IllegalArgumentException {
        if (target == null) {
            return null;
        }
        return SerialNumber.valueOf(target);
    }
}