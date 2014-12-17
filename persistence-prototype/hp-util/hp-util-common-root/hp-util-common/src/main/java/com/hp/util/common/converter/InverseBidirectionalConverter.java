/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.Converter;

/**
 * Inversed {@link BidirectionalConverter}.
 * <p>
 * A {@link BidirectionalConverter} can convert and restore an object. Because of the generic types
 * (generic types are erased at compile time), both methods to convert must be called differently:
 * {@link BidirectionalConverter#convert(Object)} and {@link BidirectionalConverter#restore(Object)}
 * . The {@code InverseBidirectionalConverter} will allow using a {@link BidirectionalConverter} as
 * a {@link Converter} that performs the same conversion than
 * {@link BidirectionalConverter#restore(Object)}.
 * 
 * @param <T> type of the target
 * @param <S> type of the source
 * @author Fabiel Zuniga
 */
public final class InverseBidirectionalConverter<T, S> implements BidirectionalConverter<T, S> {

    private final BidirectionalConverter<S, T> delegate;

    private InverseBidirectionalConverter(BidirectionalConverter<S, T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate cannot be null");
        }
        this.delegate = delegate;
    }

    /**
     * Creates the inverse converter.
     * 
     * @param converter converter to invert
     * @return the inverse
     */
    public static <S, T> BidirectionalConverter<T, S> inverse(BidirectionalConverter<S, T> converter) {
        return new InverseBidirectionalConverter<T, S>(converter);
    }

    @Override
    public S convert(T source) {
        return this.delegate.restore(source);
    }

    @Override
    public T restore(S target) throws IllegalArgumentException {
        return this.delegate.convert(target);
    }
}
