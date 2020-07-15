/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JettyXmlGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(JettyXmlGenerator.class);

    private static final String XML_ITEM_TEMPLATE = "<Item>%s</Item>";
    private static final String EMPTY_STRING = "";
    private final String gzipTemplate;

    @Inject
    public JettyXmlGenerator(final BundleContext bundleContext) throws IOException {
        final URL url = bundleContext.getBundle().getResource("jetty-gzip-template.xml");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            gzipTemplate = reader.lines().collect(Collectors.joining("\n"));
        }
    }

    Optional<URL> generateGzipConfigXmlFile(final List<String> includedMimeTypes, final List<String> includedPaths) {
        if (gzipTemplate != null && !gzipTemplate.isEmpty()) {
            try {
                final Path path = Files.createTempFile("jetty-gzip-config", ".tmp");
                path.toFile().deleteOnExit();

                final GzipConfiguration gzipConfig = new GzipConfiguration(gzipTemplate);
                final String xml = gzipConfig.withMimeTypes(includedMimeTypes).withPaths(includedPaths).buildXML();

                Files.write(path, xml.getBytes());
                return Optional.of(path.toUri().toURL());
            } catch (final IOException e) {
                LOG.error("Failed to write temporary configuration file for jetty", e);
            }
        }
        return Optional.empty();
    }

    private static final class GzipConfiguration {
        private String template;
        private boolean isMimeTypesSet;
        private boolean isPathsSet;

        GzipConfiguration(@NonNull final String template) {
            this.template = template;
        }

        GzipConfiguration withMimeTypes(@NonNull final List<String> mimeTypes) {
            template = replacePlaceholders(template, "{includedMimeTypeItem}", mimeTypes);
            isMimeTypesSet = true;
            return this;
        }

        GzipConfiguration withPaths(@NonNull final List<String> paths) {
            template = replacePlaceholders(template, "{includedPathItem}", paths);
            isPathsSet = true;
            return this;
        }

        String buildXML() {
            if (isMimeTypesSet && isPathsSet) {
                return template;
            } else {
                throw new RuntimeException("Some placeholders has not been updated with a values.");
            }
        }

        private String replacePlaceholders(final String input, final String placeholder,
                                           final List<String> values) {
            final String formattedString;
            if (isNotEmpty(values)) {
                formattedString = values.stream().map(type -> String.format(XML_ITEM_TEMPLATE, type))
                    .collect(Collectors.joining("\n"));
            } else {
                formattedString = EMPTY_STRING;
            }
            return template.replace(placeholder, formattedString);
        }

        private static boolean isNotEmpty(final Collection<?> collection) {
            return collection != null && !collection.isEmpty();
        }
    }
}
