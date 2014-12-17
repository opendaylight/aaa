/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import com.hp.util.common.Converter;
import com.hp.util.common.type.Property;

/**
 * Utility class to override {@link Object#toString()}.
 * <p>
 * {@link Object#toString()} should returns a string representation of the object. In general, a
 * string that "textually represents" the object. The result should be a concise but informative
 * representation that is easy for a person to read.
 * <p>
 * Note that {@link Object#toString()} is used for debugging purposes (Like log files), it is not
 * meant to represent a display-able text for the object (for example, to display the information in
 * user interfaces). Java toString() implementations for basic type values should not be taken as an
 * example for composite objects:
 * <p>
 * <table>
 * <tr>
 * <th>Object</th>
 * <th>toString() result</th>
 * <th>Suitable</th>
 * <th>Comments</th>
 * <th>Better alternative</th>
 * </tr>
 * <tr>
 * <td>Integer.valueOf(1)</td>
 * <td>1</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString() result that is a numeric
 * type.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>new String("Hello World")</td>
 * <td>Hello World</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString() result that is a string.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>Boolean.TRUE</td>
 * <td>true</td>
 * <td>Yes</td>
 * <td>This is a basic data type and it is easy to see from the toString() result that is a boolean.
 * </td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>new Phone(38246188)</td>
 * <td>38246188</td>
 * <td>No</td>
 * <td>This is not a basic type but a value type object, the toString() result is ambiguous with
 * numeric values.</td>
 * <td>Phone[value=38246188]</td>
 * </tr>
 * </table>
 * 
 * @author Fabiel Zuniga
 */
public final class ObjectToStringConverter {

    private ObjectToStringConverter() {

    }

    /**
     * Converts an object to a String to be used as implementation of {@link Object#toString()}.
     *
     * @param subject object to convert to string
     * @param properties object's properties to include
     * @return a string representation of {@code subject} as described in {@link Object#toString()}
     */
    @SafeVarargs
    public static String toString(Object subject, Property<String, ?>... properties) {
        if (subject == null) {
            throw new NullPointerException("obj cannot be null");
        }

        StringBuilder str = new StringBuilder(64);
        str.append(subject.getClass().getSimpleName());
        str.append('[');
        str.append(toCsv(PropertyStringConverter.getIntance(), properties));
        str.append(']');

        return str.toString();
    }

    /**
     * Converts the given subjects to a comma separated values string.
     * <p>
     * {@link Object#toString()} will be used to convert subjects to {@link String}.
     * 
     * @param subjects subjects to convert to a comma separated values
     * @return a string representing the comma separated subjects
     */
    @SafeVarargs
    public static <T> String toCsv(T... subjects) {
        return toCsv((Converter<T, String>) null, subjects);
    }

    /**
     * Converts the given subjects to a comma separated values string.
     * 
     * @param converter converted used to convert a subject to {@link String}
     * @param subjects subjects to convert to a comma separated values
     * @return a string representing the comma separated subjects
     */
    @SafeVarargs
    public static <T> String toCsv(Converter<T, String> converter, T... subjects) {
        return toCsv(converter, Arrays.asList(subjects));
    }

    /**
     * Converts the given subjects to a comma separated values string.
     * <p>
     * {@link Object#toString()} will be used to convert subjects to {@link String}.
     * 
     * @param subjects subjects to convert to a comma separated values
     * @return a string representing the comma separated subjects
     */
    public static <T> String toCsv(Collection<T> subjects) {
        return toCsv((Converter<T, String>) null, subjects);
    }

    /**
     * Converts the given subjects to a comma separated values string.
     * 
     * @param converter converted used to convert a subject to {@link String}
     * @param subjects subjects to convert to a comma separated values
     * @return a string representing the comma separated subjects
     */
    public static <T> String toCsv(Converter<T, String> converter, Collection<T> subjects) {
        StringBuilder str = new StringBuilder(64);

        for (T subject : subjects) {
            if (subject != null) {
                if (converter != null) {
                    str.append(converter.convert(subject));
                }
                else {
                    str.append(subject.toString());
                }
            }
            else {
                str.append((String) null);
            }
            str.append(", ");
        }

        if (str.length() > 0) {
            // Deletes the last ", "
            str.delete(str.length() - 2, str.length());
        }

        return str.toString();
    }

    /**
     * Converts the given {@link Throwable} to {@link String}.
     * 
     * @param throwable throwable to convert
     * @return a {@link String} representation of the given {@code throwable}
     */
    public static String toString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Converts the given {@code stackTrace} to {@link String}.
     * 
     * @param stackTrace stackTrace to convert
     * @return a {@link String} representation of the given {@code stackTrace}
     */
    public static String toString(StackTraceElement[] stackTrace) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        for (StackTraceElement element : stackTrace) {
            printWriter.println("\tat " + element);
        }
        return stringWriter.toString();
    }

    private static class PropertyStringConverter implements Converter<Property<String, ?>, String> {

        private static final Converter<Property<String, ?>, String> INSTANCE = new PropertyStringConverter();

        public static Converter<Property<String, ?>, String> getIntance() {
            return INSTANCE;
        }

        @Override
        public String convert(Property<String, ?> source) {
            return source.getIdentity() + '=' + source.getValue();
        }
    }
}
