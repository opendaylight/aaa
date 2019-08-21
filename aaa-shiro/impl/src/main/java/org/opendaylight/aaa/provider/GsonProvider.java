/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

// Taken from https://memorynotfound.com/jaxrs-jersey-gson-serializer-deserializer/
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    private static final String PRETTY_PRINT = "pretty-print";

    private final Gson gson;
    private final Gson prettyGson;

    @Context
    private UriInfo ui;

    public GsonProvider() {
        GsonBuilder builder = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization();

        this.gson = builder.create();
        this.prettyGson = builder.setPrettyPrinting().create();
    }

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType,
                              final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }

    @Override
    public T readFrom(final Class<T> type, final Type genericType, final Annotation[] annotations,
                      final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                      final InputStream entityStream) throws IOException, WebApplicationException {

        try (InputStreamReader reader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
                               final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(final T type, final Class<?> theClass, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public void writeTo(final T type, final Class<?> theClass, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws WebApplicationException {

        try (PrintWriter printWriter = new PrintWriter(entityStream)) {
            String json;
            final MultivaluedMap<String, String> queryParameters = ui != null ? ui.getQueryParameters() : null;
            if (queryParameters != null && queryParameters.containsKey(PRETTY_PRINT)) {
                json = prettyGson.toJson(type);
            } else {
                json = gson.toJson(type);
            }
            printWriter.write(json);
            printWriter.flush();
        }
    }
}

