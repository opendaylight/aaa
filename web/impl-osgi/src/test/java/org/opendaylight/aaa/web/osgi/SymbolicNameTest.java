/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import javax.servlet.ServletException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SymbolicNameTest {
    @ParameterizedTest
    @MethodSource
    void checkSymbolicNamePositive(final String str) throws Exception {
        assertSame(str, WhiteboardWebServer.checkSymbolicName(str));
    }

    private static List<Arguments> checkSymbolicNamePositive() {
        return List.of(
            Arguments.of("a"),
            Arguments.of("z"),
            Arguments.of("abc.def"),
            Arguments.of("a-b.c_d9.x"),
            Arguments.of("_"),
            Arguments.of("-"),
            Arguments.of("-._"));
    }

    @ParameterizedTest
    @MethodSource
    void checkSymbolicNameNegative(final String str) {
        final var ex = assertThrows(ServletException.class, () -> WhiteboardWebServer.checkSymbolicName(str));
        assertEquals("WebContext name '%s' is not a symbolic-name".formatted(str), ex.getMessage());
    }

    private static List<Arguments> checkSymbolicNameNegative() {
        return List.of(
            Arguments.of(""),
            Arguments.of("\n"),
            Arguments.of("+"),
            Arguments.of("."),
            Arguments.of(".a"),
            Arguments.of("a."),
            Arguments.of("a b c"));
    }
}
