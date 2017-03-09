/*
 * Copyright (c) 2014, 2017 Red Hat, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idpmapping;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.aaa.idpmapping.Token.TokenStorageType;
import org.opendaylight.aaa.idpmapping.Token.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluate a set of rules against an assertion from an external Identity
 * Provider (IdP) mapping those assertion values to local values.
 *
 * @author John Dennis &lt;jdennis@redhat.com&gt;
 */

@Deprecated
public class RuleProcessor {

    public enum ProcessResult {
        RULE_FAIL, RULE_SUCCESS, BLOCK_CONTINUE, STATEMENT_CONTINUE
    }

    private static final Logger LOG = LoggerFactory.getLogger(RuleProcessor.class);

    public String ruleIdFormat = "<rule [${rule_number}:\"${rule_name}\"]>";
    public String statementIdFormat = "<rule [${rule_number}:\"${rule_name}\"] "
            + "block [${block_number}:\"${block_name}\"] " + "statement ${statement_number}>";

    /*
     * Reserved variables
     */
    public static final String ASSERTION = "assertion";
    public static final String RULE_NUMBER = "rule_number";
    public static final String RULE_NAME = "rule_name";
    public static final String BLOCK_NUMBER = "block_number";
    public static final String BLOCK_NAME = "block_name";
    public static final String STATEMENT_NUMBER = "statement_number";
    public static final String REGEXP_ARRAY_VARIABLE = "regexp_array";
    public static final String REGEXP_MAP_VARIABLE = "regexp_map";

    private static final String REGEXP_NAMED_GROUP_PAT = "\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>";
    private static final Pattern REGEXP_NAMED_GROUP_RE = Pattern.compile(REGEXP_NAMED_GROUP_PAT);

    List<Map<String, Object>> rules = null;
    boolean success = true;
    Map<String, Map<String, Object>> mappings = null;

    public RuleProcessor(java.io.Reader rulesIn, Map<String, Map<String, Object>> mappings) {
        this.mappings = mappings;
        IdpJson json = new IdpJson();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> loadJson = (List<Map<String, Object>>) json.loadJson(rulesIn);
        rules = loadJson;
    }

    public RuleProcessor(Path rulesIn, Map<String, Map<String, Object>> mappings) throws IOException {
        this.mappings = mappings;
        IdpJson json = new IdpJson();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> loadJson = (List<Map<String, Object>>) json.loadJson(rulesIn);
        rules = loadJson;
    }

    public RuleProcessor(String rulesIn, Map<String, Map<String, Object>> mappings) {
        this.mappings = mappings;
        IdpJson json = new IdpJson();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> loadJson = (List<Map<String, Object>>) json.loadJson(rulesIn);
        rules = loadJson;
    }

    /*
     * For some odd reason the Java Regular Expression API does not include a
     * way to retrieve a map of the named groups and their values. The API only
     * permits us to retrieve a named group if we already know the group names.
     * So instead we parse the pattern string looking for named groups, extract
     * the name, look up the value of the named group and build a map from that.
     */

    private Map<String, String> regexpGroupMap(String pattern, Matcher matcher) {
        Map<String, String> groupMap = new HashMap<>();
        Matcher groupMatcher = REGEXP_NAMED_GROUP_RE.matcher(pattern);

        while (groupMatcher.find()) {
            String groupName = groupMatcher.group(1);

            groupMap.put(groupName, matcher.group(groupName));
        }
        return groupMap;
    }

    public static String join(List<Object> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object item : list) {
            if (first) {
                first = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item.toString());
        }
        return sb.toString();
    }

    private List<String> regexpGroupList(Matcher matcher) {
        List<String> groupList = new ArrayList<>(matcher.groupCount() + 1);
        groupList.add(0, matcher.group(0));
        for (int i = 1; i < matcher.groupCount() + 1; i++) {
            groupList.add(i, matcher.group(i));
        }
        return groupList;
    }

    private String objToString(Object obj) {
        StringWriter sw = new StringWriter();
        objToStringItem(sw, obj);
        return sw.toString();
    }

    private void objToStringItem(StringWriter sw, Object obj) {
        // ordered by expected occurrence
        if (obj instanceof String) {
            sw.write('"');
            sw.write(((String) obj).replaceAll("\"", "\\\""));
            sw.write('"');
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            boolean first = true;

            sw.write('[');
            for (Object item : list) {
                if (first) {
                    first = false;
                } else {
                    sw.write(", ");
                }
                objToStringItem(sw, item);
            }
            sw.write(']');
        } else if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            boolean first = true;

            sw.write('{');
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (first) {
                    first = false;
                } else {
                    sw.write(", ");
                }

                objToStringItem(sw, key);
                sw.write(": ");
                Object value = entry.getValue();
                objToStringItem(sw, value);
            }
            sw.write('}');
        } else if (obj instanceof Long) {
            sw.write(((Long) obj).toString());
        } else if (obj instanceof Boolean) {
            sw.write(((Boolean) obj).toString());
        } else if (obj == null) {
            sw.write("null");
        } else if (obj instanceof Double) {
            sw.write(((Double) obj).toString());
        } else {
            throw new IllegalStateException(String.format(
                    "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                    obj.getClass().getSimpleName()));
        }
    }

    private Object deepCopy(Object obj) {
        // ordered by expected occurrence
        if (obj instanceof String) {
            return obj; // immutable
        } else if (obj instanceof List) {
            List<Object> newList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            for (Object item : list) {
                newList.add(deepCopy(item));
            }
            return newList;
        } else if (obj instanceof Map) {
            Map<String, Object> newMap = new LinkedHashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey(); // immutable
                Object value = entry.getValue();
                newMap.put(key, deepCopy(value));
            }
            return newMap;
        } else if (obj instanceof Long) {
            return obj; // immutable
        } else if (obj instanceof Boolean) {
            return obj; // immutable
        } else if (obj == null) {
            return null;
        } else if (obj instanceof Double) {
            return obj; // immutable
        } else {
            throw new IllegalStateException(String.format(
                    "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                    obj.getClass().getSimpleName()));
        }
    }

    public String ruleId(Map<String, Object> namespace) {
        return substituteVariables(ruleIdFormat, namespace);
    }

    public String statementId(Map<String, Object> namespace) {
        return substituteVariables(statementIdFormat, namespace);
    }

    public String substituteVariables(String string, Map<String, Object> namespace) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = Token.VARIABLE_RE.matcher(string);

        while (matcher.find()) {
            Token token = new Token(matcher.group(0), namespace);
            token.load();
            String replacement;
            if (token.type == TokenType.STRING) {
                replacement = token.getStringValue();
            } else {
                replacement = objToString(token.getObjectValue());
            }

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    Map<String, Object> getMapping(Map<String, Object> namespace, Map<String, Object> rule) {
        Map<String, Object> mapping = null;
        String mappingName;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rule.get("mapping");
            mapping = map;
        } catch (java.lang.ClassCastException e) {
            throw new InvalidRuleException(
                    String.format("%s rule defines 'mapping' but it is not a Map", this.ruleId(namespace), e));
        }
        if (mapping != null) {
            return mapping;
        }
        try {
            mappingName = (String) rule.get("mapping_name");
        } catch (java.lang.ClassCastException e) {
            throw new InvalidRuleException(
                    String.format("%s rule defines 'mapping_name' but it is not a string", this.ruleId(namespace), e));
        }
        if (mappingName == null) {
            throw new InvalidRuleException(String.format(
                    "%s rule does not define mapping nor mapping_name unable to load mapping", this.ruleId(namespace)));
        }
        mapping = this.mappings.get(mappingName);
        if (mapping == null) {
            throw new InvalidRuleException(
                    String.format("%s rule specifies mapping_name '%s' but a mapping by that name does not exist,"
                            + " unable to load mapping", this.ruleId(namespace)));
        }
        LOG.debug(String.format("using named mapping '%s' from rule %s mapping=%s", mappingName, this.ruleId(namespace),
                mapping));
        return mapping;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private String getVerb(List<Object> statement) {
        Token verb;

        if (statement.size() < 1) {
            throw new InvalidRuleException("statement has no verb");
        }

        try {
            verb = new Token(statement.get(0), null);
        } catch (Exception e) {
            throw new InvalidRuleException(String.format("statement first member (i.e. verb) error %s", e));
        }

        if (verb.type != TokenType.STRING) {
            throw new InvalidRuleException(
                    String.format("statement first member (i.e. verb) must be a string, not %s", verb.type));
        }

        return verb.getStringValue().toLowerCase();
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Token getToken(String verb, List<Object> statement, int index, Map<String, Object> namespace,
            Set<TokenStorageType> storageTypes, Set<TokenType> tokenTypes) {
        Object item;
        Token token;

        try {
            item = statement.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidRuleException(
                    String.format("verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
                            statement.size(), e));
        }

        try {
            token = new Token(item, namespace);
        } catch (Exception e) {
            throw new StatementErrorException(String.format("parameter %d, %s", index, e));
        }

        if (storageTypes != null) {
            if (!storageTypes.contains(token.storageType)) {
                throw new InvalidTypeException(
                        String.format("verb '%s' requires parameter #%d to have storage types %s not %s. statement=%s",
                                verb, index, storageTypes, statement));
            }
        }

        if (tokenTypes != null) {
            token.load(); // Note, Token.load() sets the Token.type

            if (!tokenTypes.contains(token.type)) {
                throw new InvalidTypeException(
                        String.format("verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb,
                                index, tokenTypes, statement));
            }
        }

        return token;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Token getParameter(String verb, List<Object> statement, int index, Map<String, Object> namespace,
            Set<TokenType> tokenTypes) {
        Object item;
        Token token;

        try {
            item = statement.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidRuleException(
                    String.format("verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
                            statement.size(), e));
        }

        try {
            token = new Token(item, namespace);
        } catch (Exception e) {
            throw new StatementErrorException(String.format("parameter %d, %s", index, e));
        }

        token.load();

        if (tokenTypes != null) {
            try {
                token.get(); // Note, Token.get() sets the Token.type
            } catch (UndefinedValueException e) {
                // OK if not yet defined
            }
            if (!tokenTypes.contains(token.type)) {
                throw new InvalidTypeException(
                        String.format("verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb,
                                index, tokenTypes, item.getClass().getSimpleName(), statement));
            }
        }

        return token;
    }

    private Object getRawParameter(String verb, List<Object> statement, int index, Set<TokenType> tokenTypes) {
        Object item;

        try {
            item = statement.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidRuleException(
                    String.format("verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
                            statement.size(), e));
        }

        if (tokenTypes != null) {
            TokenType itemType = Token.classify(item);

            if (!tokenTypes.contains(itemType)) {
                throw new InvalidTypeException(
                        String.format("verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb,
                                index, tokenTypes, statement));
            }
        }

        return item;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Token getVariable(String verb, List<Object> statement, int index, Map<String, Object> namespace) {
        Object item;
        Token token;

        try {
            item = statement.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidRuleException(
                    String.format("verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
                            statement.size(), e));
        }

        try {
            token = new Token(item, namespace);
        } catch (Exception e) {
            throw new StatementErrorException(String.format("parameter %d, %s", index, e));
        }

        if (token.storageType != TokenStorageType.VARIABLE) {
            throw new InvalidTypeException(
                    String.format("verb '%s' requires parameter #%d to be a variable not %s. statement=%s", verb, index,
                            token.storageType, statement));
        }

        return token;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    public Map<String, Object> process(String assertionJson) {
        ProcessResult result;
        IdpJson json = new IdpJson();
        @SuppressWarnings("unchecked")
        Map<String, Object> assertion = (Map<String, Object>) json.loadJson(assertionJson);
        LOG.info("Assertion JSON: {}", json.dumpJson(assertion));
        this.success = true;

        for (int ruleNumber = 0; ruleNumber < this.rules.size(); ruleNumber++) {
            Map<String, Object> namespace = new HashMap<>();
            namespace.put(RULE_NUMBER, Long.valueOf(ruleNumber));
            namespace.put(RULE_NAME, "");
            namespace.put(ASSERTION, deepCopy(assertion));

            Map<String, Object> rule = this.rules.get(ruleNumber);
            result = processRule(namespace, rule);

            if (result == ProcessResult.RULE_SUCCESS) {
                Map<String, Object> mapped = new LinkedHashMap<>();
                Map<String, Object> mapping = getMapping(namespace, rule);
                for (Map.Entry<String, Object> entry : mapping.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Object newValue = null;
                    try {
                        Token token = new Token(value, namespace);
                        newValue = token.get();
                    } catch (Exception e) {
                        throw new InvalidRuleException(String.format("%s unable to get value for mapping %s=%s, %s",
                                ruleId(namespace), key, value, e), e);
                    }
                    mapped.put(key, newValue);
                }
                return mapped;
            }
        }
        return null;
    }

    private ProcessResult processRule(Map<String, Object> namespace, Map<String, Object> rule) {
        ProcessResult result = ProcessResult.BLOCK_CONTINUE;
        @SuppressWarnings("unchecked")
        List<List<List<Object>>> statementBlocks = (List<List<List<Object>>>) rule.get("statement_blocks");
        if (statementBlocks == null) {
            throw new InvalidRuleException("rule missing 'statement_blocks'");

        }
        for (int blockNumber = 0; blockNumber < statementBlocks.size(); blockNumber++) {
            List<List<Object>> block = statementBlocks.get(blockNumber);
            namespace.put(BLOCK_NUMBER, Long.valueOf(blockNumber));
            namespace.put(BLOCK_NAME, "");

            result = processBlock(namespace, block);
            if (EnumSet.of(ProcessResult.RULE_SUCCESS, ProcessResult.RULE_FAIL).contains(result)) {
                break;
            } else if (result == ProcessResult.BLOCK_CONTINUE) {
                continue;
            } else {
                throw new IllegalStateException(String.format("%s unexpected statement result: %s", result));
            }
        }
        if (EnumSet.of(ProcessResult.RULE_SUCCESS, ProcessResult.BLOCK_CONTINUE).contains(result)) {
            return ProcessResult.RULE_SUCCESS;
        } else {
            return ProcessResult.RULE_FAIL;
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult processBlock(Map<String, Object> namespace, List<List<Object>> block) {
        ProcessResult result = ProcessResult.STATEMENT_CONTINUE;

        for (int statementNumber = 0; statementNumber < block.size(); statementNumber++) {
            List<Object> statement = block.get(statementNumber);
            namespace.put(STATEMENT_NUMBER, Long.valueOf(statementNumber));

            try {
                result = processStatement(namespace, statement);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("%s statement=%s %s", statementId(namespace), statement, e), e);
            }
            if (EnumSet.of(ProcessResult.BLOCK_CONTINUE, ProcessResult.RULE_SUCCESS, ProcessResult.RULE_FAIL)
                    .contains(result)) {
                break;
            } else if (result == ProcessResult.STATEMENT_CONTINUE) {
                continue;
            } else {
                throw new IllegalStateException(String.format("%s unexpected statement result: %s", result));
            }
        }
        if (result == ProcessResult.STATEMENT_CONTINUE) {
            result = ProcessResult.BLOCK_CONTINUE;
        }
        return result;
    }

    private ProcessResult processStatement(Map<String, Object> namespace, List<Object> statement) {
        ProcessResult result = ProcessResult.STATEMENT_CONTINUE;
        String verb = getVerb(statement);

        switch (verb) {
            case "set":
                result = verbSet(verb, namespace, statement);
                break;
            case "length":
                result = verbLength(verb, namespace, statement);
                break;
            case "interpolate":
                result = verbInterpolate(verb, namespace, statement);
                break;
            case "append":
                result = verbAppend(verb, namespace, statement);
                break;
            case "unique":
                result = verbUnique(verb, namespace, statement);
                break;
            case "split":
                result = verbSplit(verb, namespace, statement);
                break;
            case "join":
                result = verbJoin(verb, namespace, statement);
                break;
            case "lower":
                result = verbLower(verb, namespace, statement);
                break;
            case "upper":
                result = verbUpper(verb, namespace, statement);
                break;
            case "in":
                result = verbIn(verb, namespace, statement);
                break;
            case "not_in":
                result = verbNotIn(verb, namespace, statement);
                break;
            case "compare":
                result = verbCompare(verb, namespace, statement);
                break;
            case "regexp":
                result = verbRegexp(verb, namespace, statement);
                break;
            case "regexp_replace":
                result = verbRegexpReplace(verb, namespace, statement);
                break;
            case "exit":
                result = verbExit(verb, namespace, statement);
                break;
            case "continue":
                result = verbContinue(verb, namespace, statement);
                break;
            default:
                throw new InvalidRuleException(String.format("unknown verb '%s'", verb));
        }
        return result;
    }

    private ProcessResult verbSet(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token parameter = getParameter(verb, statement, 2, namespace, null);

        variable.set(parameter.getObjectValue());
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s", statementId(namespace), verb,
                    this.success, variable, variable.get()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbLength(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token parameter = getParameter(verb, statement, 2, namespace,
                EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));
        long length;

        switch (parameter.type) {
            case ARRAY: {
                length = parameter.getListValue().size();
            }
                break;
            case MAP: {
                length = parameter.getMapValue().size();
            }
                break;
            case STRING: {
                length = parameter.getStringValue().length();
            }
                break;
            default:
                throw new IllegalStateException(String.format("unexpected token type: %s", parameter.type));
        }

        variable.set(length);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s", statementId(namespace),
                    verb, this.success, variable, variable.get(), parameter.getObjectValue()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbInterpolate(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        String string = (String) getRawParameter(verb, statement, 2, EnumSet.of(TokenType.STRING));
        String newValue = null;

        try {
            newValue = substituteVariables(string, namespace);
        } catch (Exception e) {
            throw new InvalidValueException(
                    String.format("verb '%s' failed, variable='%s' string='%s': %s", verb, variable, string, e));
        }
        variable.set(newValue);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s string='%s'", statementId(namespace), verb,
                    this.success, variable, variable.get(), string));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbAppend(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getToken(verb, statement, 1, namespace, EnumSet.of(TokenStorageType.VARIABLE),
                EnumSet.of(TokenType.ARRAY));
        Token item = getParameter(verb, statement, 2, namespace, null);

        try {
            List<Object> list = variable.getListValue();
            list.add(item.getObjectValue());
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, variable='%s' item='%s': %s", verb,
                    variable.getObjectValue(), item.getObjectValue(), e));
        }
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s item=%s", statementId(namespace), verb,
                    this.success, variable, variable.get(), item.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbUnique(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token array = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.ARRAY));

        List<Object> newValue = new ArrayList<>();
        Set<Object> seen = new HashSet<>();

        for (Object member : array.getListValue()) {
            if (seen.contains(member)) {
                continue;
            } else {
                newValue.add(member);
                seen.add(member);
            }
        }

        variable.set(newValue);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s array=%s", statementId(namespace), verb,
                    this.success, variable, variable.get(), array.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbSplit(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token string = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
        Token pattern = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));

        Pattern regexp;
        List<String> newValue;

        try {
            regexp = Pattern.compile(pattern.getStringValue());
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, bad regular expression pattern '%s', %s",
                    verb, pattern.getObjectValue(), e));
        }
        try {
            newValue = new ArrayList<>(Arrays.asList(regexp.split(string.getStringValue())));
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, string='%s' pattern='%s', %s", verb,
                    string.getObjectValue(), pattern.getObjectValue(), e));
        }

        variable.set(newValue);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s string='%s' pattern='%s'",
                    statementId(namespace), verb, this.success, variable, variable.get(), string.getObjectValue(),
                    pattern.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbJoin(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token array = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.ARRAY));
        Token conjunction = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));
        String newValue;

        try {
            newValue = join(array.getListValue(), conjunction.getStringValue());
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, array=%s conjunction='%s', %s", verb,
                    array.getObjectValue(), conjunction.getObjectValue(), e));
        }

        variable.set(newValue);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s array='%s' conjunction='%s'",
                    statementId(namespace), verb, this.success, variable, variable.get(), array.getObjectValue(),
                    conjunction.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbLower(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token parameter = getParameter(verb, statement, 2, namespace,
                EnumSet.of(TokenType.STRING, TokenType.ARRAY, TokenType.MAP));

        try {
            switch (parameter.type) {
                case STRING: {
                    String oldValue = parameter.getStringValue();
                    String newValue;
                    newValue = oldValue.toLowerCase();
                    variable.set(newValue);
                }
                    break;
                case ARRAY: {
                    List<Object> oldValue = parameter.getListValue();
                    List<Object> newValue = new ArrayList<>(oldValue.size());
                    String oldItem;
                    String newItem;

                    for (Object item : oldValue) {
                        try {
                            oldItem = (String) item;
                        } catch (ClassCastException e) {
                            throw new InvalidValueException(
                                    String.format("verb '%s' failed, array item (%s)"
                                            + "is not a string, array=%s", verb, item,
                                            parameter.getObjectValue(), e));
                        }
                        newItem = oldItem.toLowerCase();
                        newValue.add(newItem);
                    }
                    variable.set(newValue);
                }
                    break;
                case MAP: {
                    Map<String, Object> oldValue = parameter.getMapValue();
                    Map<String, Object> newValue = new LinkedHashMap<>(oldValue.size());

                    for (Map.Entry<String, Object> entry : oldValue.entrySet()) {
                        String oldKey;
                        String newKey;
                        Object value = entry.getValue();

                        oldKey = entry.getKey();
                        newKey = oldKey.toLowerCase();
                        newValue.put(newKey, value);
                    }
                    variable.set(newValue);
                }
                    break;
                default:
                    throw new IllegalStateException(String.format("unexpected token type: %s", parameter.type));
            }
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, variable='%s' parameter='%s': %s", verb,
                    variable, parameter.getObjectValue(), e), e);
        }
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s", statementId(namespace),
                    verb, this.success, variable, variable.get(), parameter.getObjectValue()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbUpper(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token variable = getVariable(verb, statement, 1, namespace);
        Token parameter = getParameter(verb, statement, 2, namespace,
                EnumSet.of(TokenType.STRING, TokenType.ARRAY, TokenType.MAP));

        try {
            switch (parameter.type) {
                case STRING: {
                    String oldValue = parameter.getStringValue();
                    String newValue;
                    newValue = oldValue.toUpperCase();
                    variable.set(newValue);
                }
                    break;
                case ARRAY: {
                    List<Object> oldValue = parameter.getListValue();
                    List<Object> newValue = new ArrayList<>(oldValue.size());
                    String oldItem;
                    String newItem;

                    for (Object item : oldValue) {
                        try {
                            oldItem = (String) item;
                        } catch (ClassCastException e) {
                            throw new InvalidValueException(
                                    String.format("verb '%s' failed, array item (%s)"
                                            + " is not a string, array=%s", verb, item,
                                            parameter.getObjectValue(), e));
                        }
                        newItem = oldItem.toUpperCase();
                        newValue.add(newItem);
                    }
                    variable.set(newValue);
                }
                    break;
                case MAP: {
                    Map<String, Object> oldValue = parameter.getMapValue();
                    Map<String, Object> newValue = new LinkedHashMap<>(oldValue.size());

                    for (Map.Entry<String, Object> entry : oldValue.entrySet()) {
                        String oldKey;
                        String newKey;
                        Object value = entry.getValue();

                        oldKey = entry.getKey();
                        newKey = oldKey.toUpperCase();
                        newValue.put(newKey, value);
                    }
                    variable.set(newValue);
                }
                    break;
                default:
                    throw new IllegalStateException(String.format("unexpected token type: %s", parameter.type));
            }
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, variable='%s' parameter='%s': %s", verb,
                    variable, parameter.getObjectValue(), e), e);
        }
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s", statementId(namespace),
                    verb, this.success, variable, variable.get(), parameter.getObjectValue()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbIn(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token member = getParameter(verb, statement, 1, namespace, null);
        Token collection = getParameter(verb, statement, 2, namespace,
                EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));

        switch (collection.type) {
            case ARRAY: {
                this.success = collection.getListValue().contains(member.getObjectValue());
            }
                break;
            case MAP: {
                if (member.type != TokenType.STRING) {
                    throw new InvalidTypeException(
                            String.format("verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
                                    TokenType.STRING, collection.type));
                }
                this.success = collection.getMapValue().containsKey(member.getObjectValue());
            }
                break;
            case STRING: {
                if (member.type != TokenType.STRING) {
                    throw new InvalidTypeException(
                            String.format("verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
                                    TokenType.STRING, collection.type));
                }
                this.success = collection.getStringValue().contains(member.getStringValue());
            }
                break;
            default:
                throw new IllegalStateException(String.format("unexpected token type: %s", collection.type));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s member=%s collection=%s", statementId(namespace), verb,
                    this.success, member.getObjectValue(), collection.getObjectValue()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbNotIn(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token member = getParameter(verb, statement, 1, namespace, null);
        Token collection = getParameter(verb, statement, 2, namespace,
                EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));

        switch (collection.type) {
            case ARRAY: {
                this.success = !collection.getListValue().contains(member.getObjectValue());
            }
                break;
            case MAP: {
                if (member.type != TokenType.STRING) {
                    throw new InvalidTypeException(
                            String.format("verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
                                    TokenType.STRING, collection.type));
                }
                this.success = !collection.getMapValue().containsKey(member.getObjectValue());
            }
                break;
            case STRING: {
                if (member.type != TokenType.STRING) {
                    throw new InvalidTypeException(
                            String.format("verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
                                    TokenType.STRING, collection.type));
                }
                this.success = !collection.getStringValue().contains(member.getStringValue());
            }
                break;
            default:
                throw new IllegalStateException(String.format("unexpected token type: %s", collection.type));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s member=%s collection=%s", statementId(namespace), verb,
                    this.success, member.getObjectValue(), collection.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbCompare(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token left = getParameter(verb, statement, 1, namespace, null);
        Token op = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
        Token right = getParameter(verb, statement, 3, namespace, null);
        String invalidOp = "operator %s not supported for type %s";
        TokenType tokenType;
        String opValue = op.getStringValue();
        boolean result;

        if (left.type != right.type) {
            throw new InvalidTypeException(
                    String.format("verb '%s' both items must have the same type left is %s and right is %s", verb,
                            left.type, right.type));
        } else {
            tokenType = left.type;
        }

        switch (opValue) {
            case "==":
            case "!=": {
                switch (tokenType) {
                    case STRING: {
                        String leftValue = left.getStringValue();
                        String rightValue = right.getStringValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case INTEGER: {
                        Long leftValue = left.getLongValue();
                        Long rightValue = right.getLongValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case REAL: {
                        Double leftValue = left.getDoubleValue();
                        Double rightValue = right.getDoubleValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case ARRAY: {
                        List<Object> leftValue = left.getListValue();
                        List<Object> rightValue = right.getListValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case MAP: {
                        Map<String, Object> leftValue = left.getMapValue();
                        Map<String, Object> rightValue = right.getMapValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case BOOLEAN: {
                        Boolean leftValue = left.getBooleanValue();
                        Boolean rightValue = right.getBooleanValue();
                        result = leftValue.equals(rightValue);
                    }
                    break;
                    case NULL: {
                        result = left.getNullValue() == right.getNullValue();
                    }
                    break;
                    default: {
                        throw new IllegalStateException(String.format("unexpected token type: %s", tokenType));
                    }
                }
                if (opValue.equals("!=")) { // negate the sense of the test
                    result = !result;
                }
            }
                break;
            case "<":
            case ">=": {
                switch (tokenType) {
                    case STRING: {
                        String leftValue = left.getStringValue();
                        String rightValue = right.getStringValue();
                        result = leftValue.compareTo(rightValue) < 0;
                    }
                        break;
                    case INTEGER: {
                        Long leftValue = left.getLongValue();
                        Long rightValue = right.getLongValue();
                        result = leftValue < rightValue;
                    }
                        break;
                    case REAL: {
                        Double leftValue = left.getDoubleValue();
                        Double rightValue = right.getDoubleValue();
                        result = leftValue < rightValue;
                    }
                break;
                    case ARRAY:
                    case MAP:
                    case BOOLEAN:
                    case NULL: {
                        throw new InvalidRuleException(String.format(invalidOp, opValue, tokenType));
                    }
                    default: {
                        throw new IllegalStateException(String.format("unexpected token type: %s", tokenType));
                    }
                }
                if (opValue.equals(">=")) { // negate the sense of the test
                    result = !result;
                }
            }
                break;
            case ">":
            case "<=": {
                switch (tokenType) {
                    case STRING: {
                        String leftValue = left.getStringValue();
                        String rightValue = right.getStringValue();
                        result = leftValue.compareTo(rightValue) > 0;
                    }
                break;
                    case INTEGER: {
                        Long leftValue = left.getLongValue();
                        Long rightValue = right.getLongValue();
                        result = leftValue > rightValue;
                    }
                break;
                    case REAL: {
                        Double leftValue = left.getDoubleValue();
                        Double rightValue = right.getDoubleValue();
                        result = leftValue > rightValue;
                    }
                break;
                    case ARRAY:
                    case MAP:
                    case BOOLEAN:
                    case NULL: {
                        throw new InvalidRuleException(String.format(invalidOp, opValue, tokenType));
                    }
                    default: {
                        throw new IllegalStateException(String.format("unexpected token type: %s", tokenType));
                    }
                }
                if (opValue.equals("<=")) { // negate the sense of the test
                    result = !result;
                }
            }
                break;
            default: {
                throw new InvalidRuleException(
                    String.format("verb '%s' has unknown comparison operator '%s'", verb, op.getObjectValue()));
            }
        }
        this.success = result;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s left=%s op='%s' right=%s", statementId(namespace), verb,
                    this.success, left.getObjectValue(), op.getObjectValue(), right.getObjectValue()));
        }
        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbRegexp(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token string = getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
        Token pattern = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));

        Pattern regexp;
        Matcher matcher;

        try {
            regexp = Pattern.compile(pattern.getStringValue());
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, bad regular expression pattern '%s', %s",
                    verb, pattern.getObjectValue(), e));
        }
        matcher = regexp.matcher(string.getStringValue());

        if (matcher.find()) {
            this.success = true;
            namespace.put(REGEXP_ARRAY_VARIABLE, regexpGroupList(matcher));
            namespace.put(REGEXP_MAP_VARIABLE, regexpGroupMap(pattern.getStringValue(), matcher));
        } else {
            this.success = false;
            namespace.put(REGEXP_ARRAY_VARIABLE, new ArrayList<>());
            namespace.put(REGEXP_MAP_VARIABLE, new HashMap<String, Object>());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s string='%s' pattern='%s' %s=%s %s=%s",
                    statementId(namespace), verb, this.success, string.getObjectValue(), pattern.getObjectValue(),
                    REGEXP_ARRAY_VARIABLE, namespace.get(REGEXP_ARRAY_VARIABLE), REGEXP_MAP_VARIABLE,
                    namespace.get(REGEXP_MAP_VARIABLE)));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private ProcessResult verbRegexpReplace(String verb, Map<String, Object> namespace, List<Object> statement) {
        Token string = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
        Token pattern = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));
        Token replacement = getParameter(verb, statement, 4, namespace, EnumSet.of(TokenType.STRING));

        Pattern regexp;
        Matcher matcher;
        String newValue;

        try {
            regexp = Pattern.compile(pattern.getStringValue());
        } catch (Exception e) {
            throw new InvalidValueException(String.format("verb '%s' failed, bad regular expression pattern '%s', %s",
                    verb, pattern.getObjectValue(), e));
        }
        matcher = regexp.matcher(string.getStringValue());

        newValue = matcher.replaceAll(replacement.getStringValue());
        Token variable = getVariable(verb, statement, 1, namespace);
        variable.set(newValue);
        this.success = true;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s variable: %s=%s string='%s' pattern='%s' replacement='%s'",
                    statementId(namespace), verb, this.success, variable, variable.get(), string.getObjectValue(),
                    pattern.getObjectValue(), replacement.getObjectValue()));
        }

        return ProcessResult.STATEMENT_CONTINUE;
    }

    private ProcessResult verbExit(String verb, Map<String, Object> namespace, List<Object> statement) {
        ProcessResult statementResult = ProcessResult.STATEMENT_CONTINUE;

        Token exitStatusParam = getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
        Token criteriaParam = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
        String exitStatus = exitStatusParam.getStringValue().toLowerCase();
        String criteria = criteriaParam.getStringValue().toLowerCase();
        ProcessResult result;
        boolean doExit;

        if (exitStatus.equals("rule_succeeds")) {
            result = ProcessResult.RULE_SUCCESS;
        } else if (exitStatus.equals("rule_fails")) {
            result = ProcessResult.RULE_FAIL;
        } else {
            throw new InvalidRuleException(String.format("verb='%s' unknown exit status '%s'", verb, exitStatus));
        }

        if (criteria.equals("if_success")) {
            if (this.success) {
                doExit = true;
            } else {
                doExit = false;
            }
        } else if (criteria.equals("if_not_success")) {
            if (!this.success) {
                doExit = true;
            } else {
                doExit = false;
            }
        } else if (criteria.equals("always")) {
            doExit = true;
        } else if (criteria.equals("never")) {
            doExit = false;
        } else {
            throw new InvalidRuleException(String.format("verb='%s' unknown exit criteria '%s'", verb, criteria));
        }

        if (doExit) {
            statementResult = result;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s status=%s criteria=%s exiting=%s result=%s",
                    statementId(namespace), verb, this.success, exitStatus, criteria, doExit, statementResult));
        }

        return statementResult;
    }

    private ProcessResult verbContinue(String verb, Map<String, Object> namespace, List<Object> statement) {
        ProcessResult statementResult = ProcessResult.STATEMENT_CONTINUE;
        Token criteriaParam = getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
        String criteria = criteriaParam.getStringValue().toLowerCase();
        boolean doContinue;

        if (criteria.equals("if_success")) {
            if (this.success) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else if (criteria.equals("if_not_success")) {
            if (!this.success) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else if (criteria.equals("always")) {
            doContinue = true;
        } else if (criteria.equals("never")) {
            doContinue = false;
        } else {
            throw new InvalidRuleException(String.format("verb='%s' unknown continue criteria '%s'", verb, criteria));
        }

        if (doContinue) {
            statementResult = ProcessResult.BLOCK_CONTINUE;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%s verb='%s' success=%s criteria=%s continuing=%s result=%s",
                    statementId(namespace), verb, this.success, criteria, doContinue, statementResult));
        }

        return statementResult;
    }
}
