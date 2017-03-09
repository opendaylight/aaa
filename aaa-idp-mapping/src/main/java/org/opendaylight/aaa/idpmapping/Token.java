/*
 * Copyright (c) 2014, 2017 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule statements can contain variables or constants, this class encapsulates
 * those values, enforces type handling and supports reading and writing of
 * those values.
 *
 * <p>
 * Technically at the syntactic level these are not tokens. A token would have
 * finer granularity such as identifier, operator, etc. I just couldn't think of
 * a better name for how they're used here and thought token was a reasonable
 * compromise as a name.
 *
 *  @author John Dennis &lt;jdennis@redhat.com&gt;
 */
@Deprecated
class Token {
    enum TokenStorageType {
        UNKNOWN, CONSTANT, VARIABLE
    }

    enum TokenType {
        STRING, // java String
        ARRAY, // java List
        MAP, // java Map
        INTEGER, // java Long
        BOOLEAN, // java Boolean
        NULL, // java null
        REAL, // java Double
        UNKNOWN, // undefined
    }

    /*
     * Regexp to identify a variable beginning with $ Supports array notation,
     * e.g. $foo[bar] Optional delimiting braces may be used to separate
     * variable from surrounding text.
     *
     * Examples: $foo ${foo} $foo[bar] ${foo[bar] where foo is the variable name
     * and bar is the array index.
     *
     * Identifer is any alphabetic followed by alphanumeric or underscore
     */
    private static final String VARIABLE_PAT = "(?<!\\\\)\\$" + // non-escaped $
                                                                // sign
            "\\{?" + // optional delimiting brace
            "([a-zA-Z][a-zA-Z0-9_]*)" + // group 1: variable name
            "(\\[" + // group 2: optional index
            "([a-zA-Z0-9_]+)" + // group 3: array index
            "\\])?" + // end optional index
            "\\}?"; // optional delimiting brace
    public static final Pattern VARIABLE_RE = Pattern.compile(VARIABLE_PAT);
    /*
     * Requires only a variable to be present in the string but permits leading
     * and trailing whitespace.
     */
    private static final String VARIABLE_ONLY_PAT = "^\\s*" + VARIABLE_PAT + "\\s*$";
    public static final Pattern VARIABLE_ONLY_RE = Pattern.compile(VARIABLE_ONLY_PAT);

    private Object value = null;

    public Map<String, Object> namespace = null;
    public TokenStorageType storageType = TokenStorageType.UNKNOWN;
    public TokenType type = TokenType.UNKNOWN;
    public String name = null;
    public String index = null;

    Token(Object input, Map<String, Object> namespace) {
        this.namespace = namespace;
        if (input instanceof String) {
            parseVariable((String) input);
            if (this.storageType == TokenStorageType.CONSTANT) {
                this.value = input;
                this.type = classify(input);
            }
        } else {
            this.storageType = TokenStorageType.CONSTANT;
            this.value = input;
            this.type = classify(input);
        }
    }

    @Override
    public String toString() {
        if (this.storageType == TokenStorageType.CONSTANT) {
            return String.format("%s", this.value);
        } else if (this.storageType == TokenStorageType.VARIABLE) {
            if (this.index == null) {
                return String.format("$%s", this.name);
            } else {
                return String.format("$%s[%s]", this.name, this.index);
            }
        } else {
            return "UNKNOWN";
        }
    }

    void parseVariable(String string) {
        Matcher matcher = VARIABLE_ONLY_RE.matcher(string);
        if (matcher.find()) {
            String name = matcher.group(1);
            String index = matcher.group(3);

            this.storageType = TokenStorageType.VARIABLE;
            this.name = name;
            this.index = index;
        } else {
            this.storageType = TokenStorageType.CONSTANT;
        }
    }

    public static TokenType classify(Object value) {
        TokenType tokenType = TokenType.UNKNOWN;
        // ordered by expected occurrence
        if (value instanceof String) {
            tokenType = TokenType.STRING;
        } else if (value instanceof List) {
            tokenType = TokenType.ARRAY;
        } else if (value instanceof Map) {
            tokenType = TokenType.MAP;
        } else if (value instanceof Long) {
            tokenType = TokenType.INTEGER;
        } else if (value instanceof Boolean) {
            tokenType = TokenType.BOOLEAN;
        } else if (value == null) {
            tokenType = TokenType.NULL;
        } else if (value instanceof Double) {
            tokenType = TokenType.REAL;
        } else {
            throw new InvalidRuleException(String.format(
                    "Type must be String, Long, Double, Boolean, List, Map, or null, not %s",
                    value.getClass().getSimpleName(), value));
        }
        return tokenType;
    }

    Object get() {
        return get(null);
    }

    Object get(Object index) {
        Object base = null;

        if (this.storageType == TokenStorageType.CONSTANT) {
            return this.value;
        }

        if (this.namespace.containsKey(this.name)) {
            base = this.namespace.get(this.name);
        } else {
            throw new UndefinedValueException(String.format("variable '%s' not defined", this.name));
        }

        if (index == null) {
            index = this.index;
        }

        if (index == null) { // scalar types
            value = base;
        } else {
            if (base instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) base;
                Integer idx = null;

                if (index instanceof Long) {
                    idx = new Integer(((Long) index).intValue());
                } else if (index instanceof String) {
                    try {
                        idx = new Integer((String) index);
                    } catch (NumberFormatException e) {
                        throw new InvalidTypeException(
                                String.format(
                                        "variable '%s' is an array indexed by '%s',"
                                        + " however the index cannot be converted to an integer",
                                        this.name, index, e));
                    }
                } else {
                    throw new InvalidTypeException(
                            String.format(
                                    "variable '%s' is an array indexed by '%s',"
                                    + " however the index must be an integer or string not %s",
                                    this.name, index, index.getClass().getSimpleName()));
                }

                try {
                    value = list.get(idx);
                } catch (IndexOutOfBoundsException e) {
                    throw new UndefinedValueException(
                            String.format(
                                    "variable '%s' is an array of size %d indexed by '%s',"
                                    + " however the index is out of bounds",
                                    this.name, list.size(), idx, e));
                }
            } else if (base instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) base;
                String idx = null;
                if (index instanceof String) {
                    idx = (String) index;
                } else {
                    throw new InvalidTypeException(
                            String.format(
                                    "variable '%s' is a map indexed by '%s', however the index must be a string not %s",
                                    this.name, index, index.getClass().getSimpleName()));
                }
                if (!map.containsKey(idx)) {
                    throw new UndefinedValueException(
                            String.format(
                                    "variable '%s' is a map indexed by '%s', however the index does not exist",
                                    this.name, index));
                }
                value = map.get(idx);
            } else {
                throw new InvalidTypeException(
                        String.format(
                                "variable '%s' is indexed by '%s', variable must be an array or map, not %s",
                                this.name, index, base.getClass().getSimpleName()));

            }
        }
        this.type = classify(value);
        return value;
    }

    void set(Object value) {
        set(value, null);
    }

    void set(Object value, Object index) {

        if (this.storageType == TokenStorageType.CONSTANT) {
            throw new InvalidTypeException("cannot assign to a constant");
        }

        if (index == null) {
            index = this.index;
        }

        if (index == null) { // scalar types
            this.namespace.put(this.name, value);
        } else {
            Object base = null;

            if (this.namespace.containsKey(this.name)) {
                base = this.namespace.get(this.name);
            } else {
                throw new UndefinedValueException(String.format("variable '%s' not defined",
                        this.name));
            }

            if (base instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) base;
                Integer idx = null;

                if (index instanceof Long) {
                    idx = new Integer(((Long) index).intValue());
                } else if (index instanceof String) {
                    try {
                        idx = new Integer((String) index);
                    } catch (NumberFormatException e) {
                        throw new InvalidTypeException(
                                String.format(
                                        "variable '%s' is an array indexed by '%s',"
                                        + " however the index cannot be converted to an integer",
                                        this.name, index, e));
                    }
                } else {
                    throw new InvalidTypeException(
                            String.format(
                                    "variable '%s' is an array indexed by '%s',"
                                    + " however the index must be an integer or string not %s",
                                    this.name, index, index.getClass().getSimpleName()));
                }

                try {
                    value = list.set(idx, value);
                } catch (IndexOutOfBoundsException e) {
                    throw new UndefinedValueException(
                            String.format(
                                    "variable '%s' is an array of size %d indexed by '%s',"
                                    + " however the index is out of bounds",
                                    this.name, list.size(), idx, e));
                }
            } else if (base instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) base;
                String idx = null;
                if (index instanceof String) {
                    idx = (String) index;
                } else {
                    throw new InvalidTypeException(
                            String.format(
                                    "variable '%s' is a map indexed by '%s', however the index must be a string not %s",
                                    this.name, index, index.getClass().getSimpleName()));
                }
                if (!map.containsKey(idx)) {
                    throw new UndefinedValueException(
                            String.format(
                                    "variable '%s' is a map indexed by '%s', however the index does not exist",
                                    this.name, index));
                }
                value = map.put(idx, value);
            } else {
                throw new InvalidTypeException(
                        String.format(
                                "variable '%s' is indexed by '%s', variable must be an array or map, not %s",
                                this.name, index, base.getClass().getSimpleName()));

            }
        }
    }

    public Object load() {
        this.value = get();
        return this.value;
    }

    public Object load(Object index) {
        this.value = get(index);
        return this.value;
    }

    public String getStringValue() {
        if (this.type == TokenType.STRING) {
            return (String) this.value;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.STRING, this.type));
        }
    }

    public List<Object> getListValue() {
        if (this.type == TokenType.ARRAY) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) this.value;
            return list;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.ARRAY, this.type));
        }
    }

    public Map<String, Object> getMapValue() {
        if (this.type == TokenType.MAP) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) this.value;
            return map;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.MAP, this.type));
        }
    }

    public Long getLongValue() {
        if (this.type == TokenType.INTEGER) {
            return (Long) this.value;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.INTEGER, this.type));
        }
    }

    public Boolean getBooleanValue() {
        if (this.type == TokenType.BOOLEAN) {
            return (Boolean) this.value;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.BOOLEAN, this.type));
        }
    }

    public Double getDoubleValue() {
        if (this.type == TokenType.REAL) {
            return (Double) this.value;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.REAL, this.type));
        }
    }

    public Object getNullValue() {
        if (this.type == TokenType.NULL) {
            return this.value;
        } else {
            throw new InvalidTypeException(String.format("expected %s value but token type is %s",
                    TokenType.NULL, this.type));
        }
    }

    public Object getObjectValue() {
        return this.value;
    }
}
