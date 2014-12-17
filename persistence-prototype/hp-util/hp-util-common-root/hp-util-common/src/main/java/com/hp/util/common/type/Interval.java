/*
 * Copyright (c) 2010 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.io.Serializable;

import com.hp.util.common.Container;
import com.hp.util.common.Converter;
import com.hp.util.common.converter.ObjectToStringConverter;

/**
 * Interval.
 * <P>
 * The intervals (of real numbers) can be classified into different types. Let {@code a} and
 * {@code b} be real numbers where {@code a < b}:
 * <UL>
 * <LI>Empty: <code> (a, a) = (a, a] = [a, a) = {} = empty</code>. {@code [b, a]} is considered as
 * an empty interval in the literature, but this class do not support it
 * <LI>Degenerate: <code>[a, a] = {a}<code>
 * <LI> Open: <code>(a, b) = {x | a < x < b}<code>
 * <LI> Closed: <code>[a, b] = {x | a <= x <= b}<code>
 * <LI> Left-open, right-closed: <code>(a, b] = {x | a < x <= b}<code>
 * <LI> Left-closed, right-open: <code>[a, b) = {x | a <= x < b}<code>
 * <LI> Left open right unbounded: <code>(a, infinite) = {x | x > a}<code>
 * <LI> Left closed right unbounded: <code>[a, infinite) = {x | x >= a}<code>
 * <LI> Left unbounded right open: <code>(infinite, b) = {x | x < b}<code>
 * <LI> Left unbounded right closed: <code>(infinite, b] = {x | x <= b}<code>
 * <LI> Unbounded: <code>(infinite, infinite) = {x | x is a real number}<code>
 * </UL>
 * 
 * @param <C> Type of the interval domain
 * @author Fabiel Zuniga
 */
public final class Interval<C extends Comparable<C>> implements Container<C>, Serializable {
    private static final long serialVersionUID = 1L;

    private final C leftEndpoint;
    private final C rightEndpoint;
    private final Type type;

    /**
     * Creates a open interval: <code>(a, b) = {x | a < x < b}<code>
     *
     * @param leftEndpoint Left endpoint
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> open(T leftEndpoint, T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, Type.OPEN);
    }

    /**
     * Creates a closed interval: <code>[a, b] = {x | a <= x <= b}<code>
     *
     * @param leftEndpoint Left endpoint
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> closed(T leftEndpoint, T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, Type.CLOSED);
    }

    /**
     * Creates a left-open right-closed interval: <code>(a, b] = {x | a <= x < b}<code>
     *
     * @param leftEndpoint Left endpoint
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftOpenRightClosed(T leftEndpoint, T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, Type.LEFT_OPEN_RIGHT_CLOSED);
    }

    /**
     * Creates a left-closed right-open interval: <code>(a, b] = {x | a < x <= b}<code>
     *
     * @param leftEndpoint Left endpoint
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftClosedRightOpen(T leftEndpoint, T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, Type.LEFT_CLOSED_RIGHT_OPEN);
    }

    /**
     * Creates a left open right unbounded interval: <code>(a, infinite) = {x | x > a}<code>
     *
     * @param leftEndpoint Left endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftOpenRightUnbounded(T leftEndpoint) {
        return new Interval<T>(leftEndpoint, null, Type.LEFT_OPEN_RIGHT_UNBOUNDED);
    }

    /**
     * Creates a Left closed right unbounded interval: <code>[a, infinite) = {x | x >= a}<code>
     *
     * @param leftEndpoint Left endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftClosedRightUnbounded(T leftEndpoint) {
        return new Interval<T>(leftEndpoint, null, Type.LEFT_CLOSED_RIGHT_UNBOUNDED);
    }

    /**
     * Creates a left unbounded right open interval: <code>(infinite, b) = {x | x < b}<code>
     *
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftUnboundedRightOpen(T rightEndpoint) {
        return new Interval<T>(null, rightEndpoint, Type.LEFT_UNBOUNDED_RIGHT_OPEN);
    }

    /**
     * Creates a left unbounded right closed interval: <code>(infinite, b] = {x | x <= b}<code>
     *
     * @param rightEndpoint Right endpoint
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> leftUnboundedRightClosed(T rightEndpoint) {
        return new Interval<T>(null, rightEndpoint, Type.LEFT_UNBOUNDED_RIGHT_CLOSED);
    }

    /**
     * Creates an unbounded interval: <code>(infinite, infinite) = {x | x is comparable}<code>
     *
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> unbounded() {
        return new Interval<T>(null, null, Type.UNBOUNDED);
    }

    /**
     * Creates an open-type interval based on the nullability of {@code leftEndpoint} and
     * {@code rightEndpoint}:
     * <ul>
     * <li>Open: if neither {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * <li>Left unbounded right open: if {@code leftEndpoint} is {@code null} but
     * {@code rightEndpoint} isn't</li>
     * <li>Left open right unbounded: if {@code leftEndpoint} is not {@code null} but
     * {@code rightEndpoint} is</li>
     * <li>Unbounded: if both {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * </ul>
     *
     * @param leftEndpoint left endpoint
     * @param rightEndpoint right endpoint
     * @return one of the open-type intervals
     * @throws IllegalArgumentException if neither {@code leftEndpoint} and {@code rightEndpoint}
     *             are {@code null} and {@code leftEndpoint} is be greater than
     *             {@code rightEndpoint}
     */
    public static <T extends Comparable<T>> Interval<T> createOpen(T leftEndpoint, T rightEndpoint)
            throws IllegalArgumentException {
        Interval<T> interval = null;
        if (leftEndpoint == null) {
            if (rightEndpoint == null) {
                interval = Interval.unbounded();
            }
            else {
                interval = Interval.leftUnboundedRightOpen(rightEndpoint);
            }
        }
        else if (rightEndpoint == null) {
            interval = Interval.leftOpenRightUnbounded(leftEndpoint);
        }
        else {
            interval = Interval.open(leftEndpoint, rightEndpoint);
        }

        return interval;
    }

    /**
     * Creates a closed-type interval based on the nullability of {@code leftEndpoint} and
     * {@code rightEndpoint}:
     * <ul>
     * <li>Closed: if neither {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * <li>Left unbounded right closed: if {@code leftEndpoint} is {@code null} but
     * {@code rightEndpoint} isn't</li>
     * <li>Left closed right unbounded: if {@code leftEndpoint} is not {@code null} but
     * {@code rightEndpoint} is</li>
     * <li>Unbounded: if both {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * </ul>
     *
     * @param leftEndpoint left endpoint
     * @param rightEndpoint right endpoint
     * @return one of the closed-type intervals
     * @throws IllegalArgumentException if neither {@code leftEndpoint} and {@code rightEndpoint}
     *             are {@code null} and {@code leftEndpoint} is be greater than
     *             {@code rightEndpoint}
     */
    public static <T extends Comparable<T>> Interval<T> createClosed(T leftEndpoint, T rightEndpoint)
            throws IllegalArgumentException {
        Interval<T> interval = null;
        if (leftEndpoint == null) {
            if (rightEndpoint == null) {
                interval = Interval.unbounded();
            }
            else {
                interval = Interval.leftUnboundedRightClosed(rightEndpoint);
            }
        }
        else if (rightEndpoint == null) {
            interval = Interval.leftClosedRightUnbounded(leftEndpoint);
        }
        else {
            interval = Interval.closed(leftEndpoint, rightEndpoint);
        }

        return interval;
    }

    Interval(C leftEndpoint, C rightEndpoint, Type type) throws NullPointerException,
            IllegalArgumentException {
        if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }

        type.validate(leftEndpoint, rightEndpoint);

        this.type = type;
        this.leftEndpoint = leftEndpoint;
        this.rightEndpoint = rightEndpoint;
    }

    /**
     * Gets the interval left endpoint.
     *
     * @return the interval left endpoint
     */
    public C getLeftEndpoint() {
        return isLeftUnbounded() ? null : this.leftEndpoint;
    }

    /**
     * Gets the interval rigth endpoint.
     *
     * @return the interval rigth endpoint
     */
    public C getRightEndpoint() {
        return isRigthUnbounded() ? null : this.rightEndpoint;
    }

    /**
     * Gets the interval type.
     *
     * @return The interval type
     */
    public Type getType() {
        return this.type;
    }

    @Override
    public boolean contains(C element) {
        return element != null && this.type.contains(element, this.leftEndpoint, this.rightEndpoint);
    }

    /**
     * Verifies whether this interval is empty:
     * <p>
     * a, b are real numbers with a < b
     * <p>
     * empty: [b, a] = (a, a) = [a, a) = (a, a] = {}
     *
     * @return True if this interval is empty, false otherwise
     */
    public boolean isEmpty() {
        return !isLeftUnbounded() && !isRigthUnbounded() && this.leftEndpoint.compareTo(this.rightEndpoint) == 0;
    }

    private boolean isLeftUnbounded() {
        return this.type == Type.UNBOUNDED || this.type == Type.LEFT_UNBOUNDED_RIGHT_OPEN
            || this.type == Type.LEFT_UNBOUNDED_RIGHT_CLOSED;
    }

    private boolean isRigthUnbounded() {
        return this.type == Type.UNBOUNDED || this.type == Type.LEFT_OPEN_RIGHT_UNBOUNDED
            || this.type == Type.LEFT_CLOSED_RIGHT_UNBOUNDED;
    }

    /**
     * Converts this interval to a different value type.
     * 
     * @param converter end point converter
     * @return converted interval
     * @throws NullPointerException if {@code converter is null}
     */
    public <T extends Comparable<T>> Interval<T> convert(Converter<C, T> converter) throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }
        return new Interval<T>(converter.convert(this.leftEndpoint), converter.convert(this.rightEndpoint), this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Interval<?>)) {
            return false;
        }

        Interval<?> other = (Interval<?>)obj;

        if (this.type != other.type) {
            return false;
        }

        if (this.leftEndpoint == null) {
            if (other.leftEndpoint != null) {
                return false;
            }
        }
        else if (!this.leftEndpoint.equals(other.leftEndpoint)) {
            return false;
        }

        if (this.rightEndpoint == null) {
            if (other.rightEndpoint != null) {
                return false;
            }
        }
        else if (!this.rightEndpoint.equals(other.rightEndpoint)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = this.type.hashCode();
        result = prime * result + ((this.leftEndpoint == null) ? 0 : this.leftEndpoint.hashCode());
        result = prime * result + ((this.rightEndpoint == null) ? 0 : this.rightEndpoint.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("leftEndpoint", this.leftEndpoint),
                Property.valueOf("rightEndpoint", this.rightEndpoint),
                Property.valueOf("type", this.type)
        );
    }

    /**
     * Interval type.
     */
    public static enum Type {

        /**
         * An open interval does not include its end points, and is indicated with parentheses. For
         * example {@code (0,1)} means greater than {@code 0} and less than {@code 1}.
         */
        OPEN {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }

                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }

                if (leftEndpoint.compareTo(rightEndpoint) > 0) {
                    throw new IllegalArgumentException("leftEndpoint cannot be greater than rightEndpoint");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) > 0 && element.compareTo(rightEndpoint) < 0;
            }
        },

        /**
         * A closed interval includes its end points, and is denoted with square brackets. For
         * example {@code [0,1]} means greater than or equal to {@code 0} and less than or equal to
         * {@code 1}.
         */
        CLOSED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }

                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }

                if (leftEndpoint.compareTo(rightEndpoint) > 0) {
                    throw new IllegalArgumentException("leftEndpoint cannot be greater than rightEndpoint");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) >= 0 && element.compareTo(rightEndpoint) <= 0;
            }
        },

        /**
         * An interval is said to be left-bounded or right-bounded if there is some real number that is, respectively, smaller than or larger than all its
         * elements. An interval is said to be bounded if it is both left- and right-bounded; and is said to be unbounded otherwise.
         */
        UNBOUNDED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {

            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return true;
            }
        },

        /** Left closed right open */
        LEFT_CLOSED_RIGHT_OPEN {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }

                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }

                if (leftEndpoint.compareTo(rightEndpoint) > 0) {
                    throw new IllegalArgumentException("leftEndpoint cannot be greater than rightEndpoint");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) >= 0 && element.compareTo(rightEndpoint) < 0;
            }
        },

        /** Left open right closed. */
        LEFT_OPEN_RIGHT_CLOSED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }

                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }

                if (leftEndpoint.compareTo(rightEndpoint) > 0) {
                    throw new IllegalArgumentException("leftEndpoint cannot be greater than rightEndpoint");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) > 0 && element.compareTo(rightEndpoint) <= 0;
            }
        },

        /** Left open right unbounded */
        LEFT_OPEN_RIGHT_UNBOUNDED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) > 0;
            }
        },

        /** Left closed right unbounded */
        LEFT_CLOSED_RIGHT_UNBOUNDED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (leftEndpoint == null) {
                    throw new IllegalArgumentException("leftEndpoint cannot be null");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(leftEndpoint) >= 0;
            }
        },

        /** Left unbounded right open */
        LEFT_UNBOUNDED_RIGHT_OPEN {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(rightEndpoint) < 0;
            }
        },

        /** Left unbounded right closed */
        LEFT_UNBOUNDED_RIGHT_CLOSED {
            @Override
            <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint) throws IllegalArgumentException {
                if (rightEndpoint == null) {
                    throw new IllegalArgumentException("rightEndpoint cannot be null");
                }
            }

            @Override
            <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint) {
                return element.compareTo(rightEndpoint) <= 0;
            }
        }

        ;

        abstract <C extends Comparable<C>> void validate(C leftEndpoint, C rightEndpoint)
                throws IllegalArgumentException;

        abstract <C extends Comparable<C>> boolean contains(C element, C leftEndpoint, C rightEndpoint);
    }
}
