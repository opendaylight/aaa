/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility methods for dealing with aspects of Java Servlet Specification. We currently support
 * <a href="https://github.com/javaee/servlet-spec/blob/gh-pages/downloads/servlet-3.1/Final/servlet-3_1-final.pdf">
 * version 3.1</a>.
 */
final class ServletSpec {
    private ServletSpec() {
        // utility class
    }

    /**
     * Verify that the specified string is a valid Context Path as defined in Section 3.5.
     *
     * @param str String to check
     * @return The string
     * @throws IllegalArgumentException if {@code str} is not a valid context path
     * @throws NullPointerException if {@code str} is {@code null}
     */
    static @NonNull String requireContextPath(final String str) {
        // We do not allow this:
        //   If this context is the “default” context rooted at the base of the
        //   Web server’s URL name space, this path will be an empty string.
        checkArgument(!str.isEmpty(), "Context path is empty");

        // Otherwise, if the
        // context is not rooted at the root of the server’s name space, the path starts with a
        // character but does not end with a / character.
        checkArgument(str.charAt(0) == '/', "Context path '%s' does not start with '/'", str);
        checkArgument(str.charAt(str.length() - 1) != '/', "Context path '%s' ends with '/'", str);
        return str;
    }

    /**
     * Verify that the specified string is a valid Specification of Mapping as defined in Section 12.2.
     *
     * @param str String to check
     * @return The string
     * @throws IllegalArgumentException if {@code str} is not a valid mapping specification
     * @throws NullPointerException if {@code str} is {@code null}
     */
    static @NonNull String requireMappingSpec(final String str) {
        // Bullet 3:
        //      The empty string ("") is a special URL pattern that exactly maps to the
        //      application's context root, i.e., requests of the form http://host:port/<context-
        //      root>/. In this case the path info is ’ / ’ and the servlet path and context path is
        //      empty string (““).
        if (str.isEmpty()) {
            return "";
        }

        final char firstChar = str.charAt(0);
        final int len = str.length();
        if (firstChar == '/') {
            // Bullet 4:
            //      A string containing only the ’ / ’ character indicates the "default" servlet of the
            //      application. In this case the servlet path is the request URI minus the context path
            //      and the path info is null.
            // otherwise ...
            if (len != 1) {
                // ... more checks starting at the second character
                final int star = str.indexOf('*', 1);
                checkArgument(
                    // Bullet 5:
                    //      All other strings are used for exact matches only.
                    star == -1
                    // or Bullet 1:
                    //      A string beginning with a ‘ / ’ character and ending with a ‘ /*’ suffix is used for
                    //      path mapping.
                    || star == len - 1 && str.charAt(star - 1) == '/',
                    // ... otherwise it is a '*' in an exact path
                    "Prefix-based spec '%s' with a '*' at offset %s", str, star);
            }
        } else {
            // Bullet 2:
            //      A string beginning with a ‘ *. ’ prefix is used as an extension mapping
            checkArgument(firstChar == '.' && len > 1 && str.charAt(1) == '*',
                "Spec '%s' is neither prefix-based nor suffix-based", str);

            final int slash = str.indexOf('/', 2);
            checkArgument(slash == -1, "Suffix-based spec '%s' with a '/' at offset %s", str, slash);
            final int star = str.indexOf('*', 2);
            checkArgument(star == -1, "Suffix-based spec '%s' with a '*' at offset %s", str, star);
        }

        return str;
    }
}
