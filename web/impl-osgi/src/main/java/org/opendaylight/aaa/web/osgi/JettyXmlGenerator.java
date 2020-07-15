/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class JettyXmlGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(JettyXmlGenerator.class);

    private static final String XML_ITEM_TEMPLATE = "<Item>%s</Item>";
    private static final String EMPTY_STRING = "";
    private final String gzipTemplate;

    @Inject
    public JettyXmlGenerator(final BundleContext bundleContext) throws IOException {
        final URL url = bundleContext.getBundle().getResource("jetty-gzip-template.xml");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            gzipTemplate = reader.lines().collect(Collectors.joining("\n"));
        }
    }

    URL generateGzipXmlFile(final List<String> includedMimeTypes, final List<String> includedPaths) {
        if (gzipTemplate != null && !gzipTemplate.isEmpty()) {
            try {
                final Path path = Files.createTempFile("jetty-gzip", ".tmp");
                path.toFile().deleteOnExit();

                String configuredXml = replacePlaceholders(gzipTemplate, "{includedMimeTypeItem}",
                    includedMimeTypes);
                configuredXml = replacePlaceholders(configuredXml, "{includedPathItem}", includedPaths);

                Files.write(path, configuredXml.getBytes());
                return path.toUri().toURL();
            } catch (final IOException e) {
                LOG.error("Failed to write temporary configuration file for jetty", e);
            }
        }
        return null;
    }

    private String replacePlaceholders(final String template, final String placeholder,
                                       final List<String> replacementValues) {
        final String formattedString;
        if (isNotEmpty(replacementValues)) {
            formattedString = replacementValues.stream().map(type -> String.format(XML_ITEM_TEMPLATE, type))
                .collect(Collectors.joining("\n"));
        } else {
            formattedString = EMPTY_STRING;
        }
        return template.replace(placeholder, formattedString);
    }

    protected static boolean isNotEmpty(final Collection<?> c) {
        return c != null && !c.isEmpty();
    }
}
