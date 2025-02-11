/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Integration Test for the built JAR file.
 *
 * <p>Note that the maven-failsafe-plugin, not the usual maven-surefire-plugin (for
 * *Test), runs this *IT AFTER the final "fat" self-executable JAR has been
 * built.
 *
 * @author Michael Vorburger
 */
public class MainIT {
    private static final Path DIR = Path.of("target", MainIT.class.getSimpleName());

    @Test
    public void integrationTestBuildJAR() throws Exception {
        FilesUtils.delete(DIR);
        runFatJAR("-a",
                "--nu",
                "vorburger",
                "-p",
                "nosecret");
        runFatJAR("--du",
                "vorburger");
    }

    private static void runFatJAR(final String... arguments) throws IOException, InterruptedException {
        final var fullArguments = new ArrayList<>(List.of(
            Path.of(System.getProperty("java.home")).resolve("bin").resolve("java").toAbsolutePath().toString(),
            "-jar",
            findExecutableFatJAR().toAbsolutePath().toString(),
            "--dbd",
            DIR.toString()));
        fullArguments.addAll(List.of(arguments));

        // If Output piping to LOG instead of inheritIO() etc. is needed, then
        // consider using https://github.com/vorburger/MariaDB4j/tree/master/mariaDB4j-core/src/main/java/ch/vorburger/exec
        Process process = new ProcessBuilder(fullArguments)
                .inheritIO()
                // NO .redirectErrorStream(true)
                .start();
        process.waitFor();
        assertEquals(0, process.exitValue());
    }

    private static Path findExecutableFatJAR() {
        final var targetDirectory = Path.of(".", "target").toFile();
        final var jarFiles = targetDirectory.listFiles((dir, name) -> name.startsWith("aaa-cli-jar-")
            && name.endsWith(".jar")
            && !name.contains("-javadoc")
            && !name.contains("-sources"));
        assertNotNull("*jar-with-dependencies.jar files in " + targetDirectory, jarFiles);
        assertEquals("*jar-with-dependencies.jar files in " + targetDirectory, 1, jarFiles.length);
        return jarFiles[0].toPath();
    }
}
