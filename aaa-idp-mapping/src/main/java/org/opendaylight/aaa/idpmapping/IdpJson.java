/*
 * Copyright (c) 2014, 2017 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * Converts between JSON and the internal data structures used in the
 * RuleProcessor.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class IdpJson {

    public IdpJson() {
    }

    public Object loadJson(java.io.Reader in) {
        JsonParser parser = Json.createParser(in);
        Event event = null;

        // Prime the pump. Get the first item from the parser.
        event = parser.next();

        // Act on first item.
        return loadJsonItem(parser, event);
    }

    public Object loadJson(Path filename) throws IOException {
        BufferedReader reader = Files.newBufferedReader(filename, StandardCharsets.UTF_8);
        return loadJson(reader);
    }

    public Object loadJson(String string) {
        StringReader reader = new StringReader(string);
        return loadJson(reader);
    }

    /*
     * Process current parser item indicated by event. Consumes exactly the
     * number of parser events necessary to load the item. Caller must advance
     * the parser via parser.next() after this method returns.
     */
    private Object loadJsonItem(JsonParser parser, Event event) {
        switch (event) {
            case START_OBJECT: {
                return loadJsonObject(parser, event);
            }
            case START_ARRAY: {
                return loadJsonArray(parser, event);
            }
            case VALUE_NULL: {
                return null;
            }
            case VALUE_NUMBER: {
                if (parser.isIntegralNumber()) {
                    return parser.getLong();
                } else {
                    return parser.getBigDecimal().doubleValue();
                }
            }
            case VALUE_STRING: {
                return parser.getString();
            }
            case VALUE_TRUE: {
                return Boolean.TRUE;
            }
            case VALUE_FALSE: {
                return Boolean.FALSE;
            }
            default: {
                JsonLocation location = parser.getLocation();
                throw new IllegalStateException(
                        String.format("unknown JSON parsing event %s, location(line=%d column=%d offset=%d)", event,
                                location.getLineNumber(), location.getColumnNumber(), location.getStreamOffset()));
            }
        }
    }

    private List<Object> loadJsonArray(JsonParser parser, Event event) {
        List<Object> list = new ArrayList<>();

        if (event != Event.START_ARRAY) {
            JsonLocation location = parser.getLocation();
            throw new IllegalStateException(String.format(
                    "expected JSON parsing event to be START_ARRAY, not %s location(line=%d column=%d offset=%d)",
                    event, location.getLineNumber(), location.getColumnNumber(), location.getStreamOffset()));
        }
        event = parser.next(); // consume START_ARRAY
        while (event != Event.END_ARRAY) {
            Object obj;

            obj = loadJsonItem(parser, event);
            list.add(obj);
            event = parser.next(); // next array item or END_ARRAY
        }
        return list;
    }

    private Map<String, Object> loadJsonObject(JsonParser parser, Event event) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (event != Event.START_OBJECT) {
            JsonLocation location = parser.getLocation();
            throw new IllegalStateException(String.format("expected JSON parsing event to be START_OBJECT, not %s, ",
                    "location(line=%d column=%d offset=%d)", event, location.getLineNumber(),
                    location.getColumnNumber(), location.getStreamOffset()));
        }
        event = parser.next(); // consume START_OBJECT
        while (event != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String key;
                Object value;

                key = parser.getString();
                event = parser.next(); // consume key
                value = loadJsonItem(parser, event);
                map.put(key, value);
            } else {
                JsonLocation location = parser.getLocation();
                throw new IllegalStateException(String.format(
                        "expected JSON parsing event to be KEY_NAME, not %s, location(line=%d column=%d offset=%d)",
                        event, location.getLineNumber(), location.getColumnNumber(), location.getStreamOffset()));

            }
            event = parser.next(); // next key or END_OBJECT
        }
        return map;
    }

    public String dumpJson(Object obj) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(properties);
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = generatorFactory.createGenerator(stringWriter);

        dumpJsonItem(generator, obj);
        generator.close();
        return stringWriter.toString();
    }

    private void dumpJsonItem(JsonGenerator generator, Object obj) {
        // ordered by expected occurrence
        if (obj instanceof String) {
            generator.write((String) obj);
        } else if (obj instanceof List) {
            generator.writeStartArray();
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            dumpJsonArray(generator, list);
        } else if (obj instanceof Map) {
            generator.writeStartObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            dumpJsonObject(generator, map);
        } else if (obj instanceof Long) {
            generator.write(((Long) obj).longValue());
        } else if (obj instanceof Boolean) {
            generator.write(((Boolean) obj).booleanValue());
        } else if (obj == null) {
            generator.writeNull();
        } else if (obj instanceof Double) {
            generator.write(((Double) obj).doubleValue());
        } else {
            throw new IllegalStateException(String.format(
                    "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                    obj.getClass().getSimpleName()));
        }
    }

    private void dumpJsonArray(JsonGenerator generator, List<Object> list) {
        for (Object obj : list) {
            dumpJsonItem(generator, obj);
        }
        generator.writeEnd();
    }

    private void dumpJsonObject(JsonGenerator generator, Map<String, Object> map) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object obj = entry.getValue();

            // ordered by expected occurrence
            if (obj instanceof String) {
                generator.write(key, (String) obj);
            } else if (obj instanceof List) {
                generator.writeStartArray(key);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) obj;
                dumpJsonArray(generator, list);
            } else if (obj instanceof Map) {
                generator.writeStartObject(key);
                @SuppressWarnings("unchecked")
                Map<String, Object> map1 = (Map<String, Object>) obj;
                dumpJsonObject(generator, map1);
            } else if (obj instanceof Long) {
                generator.write(key, ((Long) obj).longValue());
            } else if (obj instanceof Boolean) {
                generator.write(key, ((Boolean) obj).booleanValue());
            } else if (obj == null) {
                generator.write(key, JsonValue.NULL);
            } else if (obj instanceof Double) {
                generator.write(key, ((Double) obj).doubleValue());
            } else {
                throw new IllegalStateException(String.format(
                        "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                        obj.getClass().getSimpleName()));
            }
        }
        generator.writeEnd();
    }
}
