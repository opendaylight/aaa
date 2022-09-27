/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

final class ServletPathSpecValidator {
    private ServletPathSpecValidator() {
        // utility class
    }

    /**
     * Perform servlet path validation according to Java Servlet Specification.
     *
     * <p>
     * For more info refer to <a href="https://javaee.github.io/servlet-spec/downloads/servlet-4.0/servlet-4_0_FINAL.pdf">Java Servlet Specification</a>.
     * This method is a copy of ServletPathSpec#assertValidServletPathSpec method from <a href="https://github.com/eclipse/jetty.project">jetty.project</a>.
     *
     * @param servletPathSpec servlet URL to be validated
     */
    static void checkUrlPattern(final String servletPathSpec) {
        if ((servletPathSpec == null) || servletPathSpec.isEmpty()) {
            return; // empty path spec
        }

        int len = servletPathSpec.length();
        // path spec must either start with '/' or '*.'
        if (servletPathSpec.charAt(0) == '/') {
            // Prefix Based
            if (len == 1) {
                return; // simple '/' path spec
            }
            int idx = servletPathSpec.indexOf('*');
            if (idx < 0) {
                return; // no hit on glob '*'
            }
            // only allowed to have '*' at the end of the path spec
            if (idx != (len - 1)) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: glob '*' can only exist at end of "
                        + "prefix based matches: bad spec \"" + servletPathSpec + "\"");
            }
        } else if (servletPathSpec.startsWith("*.")) {
            // Suffix Based
            int idx = servletPathSpec.indexOf('/');
            // cannot have path separator
            if (idx >= 0) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have "
                        + "path separators: bad spec \"" + servletPathSpec + "\"");
            }

            idx = servletPathSpec.indexOf('*', 2);
            // only allowed to have 1 glob '*', at the start of the path spec
            if (idx >= 1) {
                throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have "
                        + "multiple glob '*': bad spec \"" + servletPathSpec + "\"");
            }
        } else {
            throw new IllegalArgumentException("Servlet Spec 12.2 violation: path spec must start "
                    + "with \"/\" or \"*.\": bad spec \"" + servletPathSpec + "\"");
        }
    }
}
