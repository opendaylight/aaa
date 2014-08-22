/*
 * Copyright (C) 2014 Red Hat
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idp_mapping;


import java.io.IOException;
import java.io.StringWriter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum ProcessResult {
  RULE_FAIL, RULE_SUCCESS, BLOCK_CONTINUE, STATEMENT_CONTINUE
}


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


class Token {
  private static final Logger logger = LoggerFactory
    .getLogger(Token.class);

  /*
   * Regexp to identify a variable beginning with $ Supports array notation, e.g. $foo[bar] Optional
   * delimiting braces may be used to separate variable from surrounding text.
   * 
   * Examples: $foo ${foo} $foo[bar] ${foo[bar] where foo is the variable name and bar is the array
   * index.
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
   * Requires only a variable to be present in the string but permits leading and trailing
   * whitespace.
   */
  private static final String VARIABLE_ONLY_PAT = "^\\s*" + VARIABLE_PAT + "\\s*$";
  public static final Pattern VARIABLE_ONLY_RE = Pattern.compile(VARIABLE_ONLY_PAT);

  public Map<String, Object> namespace = null;
  public TokenStorageType storageType = TokenStorageType.UNKNOWN;
  public TokenType type = TokenType.UNKNOWN;
  public Object value = null;
  public String name = null;
  public String index = null;

  Token(Object input, Map<String, Object> namespace) throws InvalidRuleException {
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

  public static TokenType classify(Object value) throws InvalidRuleException {
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
          "Type must be String, Long, Double, Boolean, List, Map, or null, not %s", value
              .getClass().getSimpleName(), value));
    }
    return tokenType;
  }

  Object get() throws UndefinedValueException, InvalidTypeException, InvalidRuleException {
    return get(null);
  }

  Object get(Object index) throws UndefinedValueException, InvalidTypeException,
      InvalidRuleException {
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
        List list = (List) base;
        Integer idx = null;

        if (index instanceof Long) {
          idx = new Integer(((Long) index).intValue());
        } else if (index instanceof String) {
          try {
            idx = new Integer((String) index);
          } catch (NumberFormatException e) {
            throw new InvalidTypeException(
                String
                    .format(
                        "variable '%s' is an array indexed by '%s', however the index cannot be converted to an integer",
                        this.name, index));
          }
        } else {
          throw new InvalidTypeException(
              String
                  .format(
                      "variable '%s' is an array indexed by '%s', however the index must be an integer or string not %s",
                      this.name, index, index.getClass().getSimpleName()));
        }

        try {
          value = list.get(idx);
        } catch (IndexOutOfBoundsException e) {
          throw new UndefinedValueException(
              String
                  .format(
                      "variable '%s' is an array of size %d indexed by '%s', however the index is out of bounds",
                      this.name, list.size(), idx));
        }
      } else if (base instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) base;
        String idx = null;
        if (index instanceof String) {
          idx = (String) index;
        } else {
          throw new InvalidTypeException(String.format(
              "variable '%s' is a map indexed by '%s', however the index must be a string not %s",
              this.name, index, index.getClass().getSimpleName()));
        }
        if (!map.containsKey(idx)) {
          throw new UndefinedValueException(String.format(
              "variable '%s' is a map indexed by '%s', however the index does not exist",
              this.name, index));
        }
        value = map.get(idx);
      } else {
        throw new InvalidTypeException(String.format(
            "variable '%s' is indexed by '%s', variable must be an array or map, not %s",
            this.name, index, base.getClass().getSimpleName()));

      }
    }
    this.type = classify(value);
    return value;
  }

  void set(Object value) throws InvalidTypeException, UndefinedValueException {
    set(value, null);
  }

  void set(Object value, Object index) throws InvalidTypeException, UndefinedValueException {

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

      if (base instanceof List) {
        List list = (List) base;
        Integer idx = null;

        if (index instanceof Long) {
          idx = new Integer(((Long) index).intValue());
        } else if (index instanceof String) {
          try {
            idx = new Integer((String) index);
          } catch (NumberFormatException e) {
            throw new InvalidTypeException(
                String
                    .format(
                        "variable '%s' is an array indexed by '%s', however the index cannot be converted to an integer",
                        this.name, index));
          }
        } else {
          throw new InvalidTypeException(
              String
                  .format(
                      "variable '%s' is an array indexed by '%s', however the index must be an integer or string not %s",
                      this.name, index, index.getClass().getSimpleName()));
        }

        try {
          value = list.set(idx, value);
        } catch (IndexOutOfBoundsException e) {
          throw new UndefinedValueException(
              String
                  .format(
                      "variable '%s' is an array of size %d indexed by '%s', however the index is out of bounds",
                      this.name, list.size(), idx));
        }
      } else if (base instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) base;
        String idx = null;
        if (index instanceof String) {
          idx = (String) index;
        } else {
          throw new InvalidTypeException(String.format(
              "variable '%s' is a map indexed by '%s', however the index must be a string not %s",
              this.name, index, index.getClass().getSimpleName()));
        }
        if (!map.containsKey(idx)) {
          throw new UndefinedValueException(String.format(
              "variable '%s' is a map indexed by '%s', however the index does not exist",
              this.name, index));
        }
        value = map.put(idx, value);
      } else {
        throw new InvalidTypeException(String.format(
            "variable '%s' is indexed by '%s', variable must be an array or map, not %s",
            this.name, index, base.getClass().getSimpleName()));

      }
    }
  }

  public Object load() throws UndefinedValueException, InvalidTypeException, InvalidRuleException {
    this.value = get();
    return this.value;
  }

  public Object load(Object index) throws UndefinedValueException, InvalidTypeException,
      InvalidRuleException {
    this.value = get(index);
    return this.value;
  }

}


public class RuleProcessor {
  private static final Logger logger = LoggerFactory
    .getLogger(RuleProcessor.class);

  public String ruleIdFormat = "<rule [${rule_number}:\"${rule_name}\"]>";
  public String statementIdFormat =
      "<rule [${rule_number}:\"${rule_name}\"] block [${block_number}:\"${block_name}\"] statement ${statement_number}>";

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


  List rules = null;
  boolean success = true;
  Map<String, Map<String, Object>> mappings = null;

  public RuleProcessor(java.io.Reader rulesIn, Map<String, Map<String, Object>> mappings) {
    this.mappings = mappings;
    IdpJson json = new IdpJson();
    rules = (List) json.loadJson(rulesIn);
  }

  public RuleProcessor(Path rulesIn, Map<String, Map<String, Object>> mappings) throws IOException {
    this.mappings = mappings;
    IdpJson json = new IdpJson();
    rules = (List) json.loadJson(rulesIn);
  }

  public RuleProcessor(String rulesIn, Map<String, Map<String, Object>> mappings) {
    this.mappings = mappings;
    IdpJson json = new IdpJson();
    rules = (List) json.loadJson(rulesIn);
  }

  /*
   * For some odd reason the Java Regular Expression API does not include a way to retrieve a map of
   * the named groups and their values. The API only permits us to retrieve a named group if we
   * already know the group names. So instead we parse the pattern string looking for named groups,
   * extract the name, look up the value of the named group and build a map from that.
   */

  private Map<String, String> regexpGroupMap(String pattern, Matcher matcher) {
    Map<String, String> groupMap = new HashMap<String, String>();
    Matcher groupMatcher = REGEXP_NAMED_GROUP_RE.matcher(pattern);

    while (groupMatcher.find()) {
      String groupName = groupMatcher.group(1);

      groupMap.put(groupName, matcher.group(groupName));
    }
    return groupMap;
  }

  static public String join(List<Object> list, String conjunction) {
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
    List<String> groupList = new ArrayList<String>(matcher.groupCount() + 1);
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
      Map<String, Object> map = (Map<String, Object>) obj;
      boolean first = true;

      sw.write('{');
      for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();

        if (first) {
          first = false;
        } else {
          sw.write(", ");
        }

        objToStringItem(sw, key);
        sw.write(": ");
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
      throw new IllegalStateException(
          String
              .format(
                  "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                  obj.getClass().getSimpleName()));
    }
  }

  // FIXME: primitive wrapper classes are immutable, no need to create new instances
  private Object deepCopy(Object obj) {
    // ordered by expected occurrence
    if (obj instanceof String) {
      return new String((String) obj);
    } else if (obj instanceof List) {
      List<Object> list = new ArrayList<Object>();
      for (Object item : (List) obj) {
        list.add(deepCopy(item));
      }
      return list;
    } else if (obj instanceof Map) {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        map.put(new String(key), deepCopy(value));
      }
      return map;
    } else if (obj instanceof Long) {
      return new Long((Long) obj);
    } else if (obj instanceof Boolean) {
      return new Boolean((Boolean) obj);
    } else if (obj == null) {
      return null;
    } else if (obj instanceof Double) {
      return new Double((Double) obj);
    } else {
      throw new IllegalStateException(
          String
              .format(
                  "unsupported data type, must be String, Long, Double, Boolean, List, Map, or null, not %s",
                  obj.getClass().getSimpleName()));
    }
  }

  public String ruleId(Map<String, Object> namespace) throws InvalidRuleException,
      UndefinedValueException, InvalidTypeException {
    return substituteVariables(ruleIdFormat, namespace);
  }

  public String statementId(Map<String, Object> namespace) throws InvalidRuleException,
      UndefinedValueException, InvalidTypeException {
    return substituteVariables(statementIdFormat, namespace);
  }

  public String substituteVariables(String string, Map<String, Object> namespace)
      throws InvalidRuleException, UndefinedValueException, InvalidTypeException {
    StringBuffer sb = new StringBuffer();
    Matcher matcher = Token.VARIABLE_RE.matcher(string);

    while (matcher.find()) {
      Token token = new Token(matcher.group(0), namespace);
      token.load();
      String replacement;
      if (token.type == TokenType.STRING) {
        replacement = (String) token.value;
      } else {
        replacement = token.value.toString();
      }

      matcher.appendReplacement(sb, replacement);
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  Map<String, Object> getMapping(Map<String, Object> namespace, Map<String, Object> rule)
      throws InvalidRuleException, UndefinedValueException, InvalidTypeException {
    Map<String, Object> mapping = null;
    String mappingName = null;

    try {
      mapping = (Map<String, Object>) rule.get("mapping");
    } catch (java.lang.ClassCastException e) {
      throw new InvalidRuleException(String.format("%s rule defines 'mapping' but it is not a Map",
          this.ruleId(namespace)));
    }
    if (mapping != null) {
      return mapping;
    }
    try {
      mappingName = (String) rule.get("mapping_name");
    } catch (java.lang.ClassCastException e) {
      throw new InvalidRuleException(String.format(
          "%s rule defines 'mapping_name' but it is not a string", this.ruleId(namespace)));
    }
    if (mappingName == null) {
      throw new InvalidRuleException(String.format(
          "%s rule does not define mapping nor mapping_name unable to load mapping",
          this.ruleId(namespace)));
    }
    mapping = this.mappings.get(mappingName);
    if (mapping == null) {
      throw new InvalidRuleException(
          String
              .format(
                  "%s rule specifies mapping_name '%s' but a mapping by that name does not exist, unable to load mapping",
                  this.ruleId(namespace)));
    }
    logger.debug(String.format("using named mapping '%s' from rule %s mapping=%s", mappingName,
        this.ruleId(namespace), mapping));
    return mapping;
  }

  private String getVerb(List<Object> statement) throws InvalidRuleException {
    Token verb;

    if (statement.size() < 1) {
      throw new InvalidRuleException("statement has no verb");
    }

    try {
      verb = new Token(statement.get(0), null);
    } catch (Exception e) {
      throw new InvalidRuleException(
          String.format("statement first member (i.e. verb) error %s", e));
    }

    if (verb.type != TokenType.STRING) {
      throw new InvalidRuleException(String.format(
          "statement first member (i.e. verb) must be a string, not %s", verb.type));
    }

    return ((String) verb.value).toLowerCase();
  }

  private Token getToken(String verb, List<Object> statement, int index,
      Map<String, Object> namespace, Set<TokenStorageType> storageTypes, Set<TokenType> tokenTypes)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException {
    Object item;
    Token token;

    try {
      item = statement.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidRuleException(String.format(
          "verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
          statement.size()));
    }

    try {
      token = new Token(item, namespace);
    } catch (Exception e) {
      throw new StatementErrorException(String.format("parameter %d, %s", index, e));
    }

    if (storageTypes != null) {
      if (!storageTypes.contains(token.storageType)) {
        throw new InvalidTypeException(String.format(
            "verb '%s' requires parameter #%d to have storage types %s not %s. statement=%s", verb,
            index, storageTypes, statement));
      }
    }

    if (tokenTypes != null) {
      token.load(); // Note, Token.load() sets the Token.type

      if (!tokenTypes.contains(token.type)) {
        throw new InvalidTypeException(String.format(
            "verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb, index,
            tokenTypes, statement));
      }
    }

    return token;
  }

  private Token getParameter(String verb, List<Object> statement, int index,
      Map<String, Object> namespace, Set<TokenType> tokenTypes) throws InvalidRuleException,
      StatementErrorException, InvalidTypeException, UndefinedValueException {
    Object item;
    Token token;

    try {
      item = statement.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidRuleException(String.format(
          "verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
          statement.size()));
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
        throw new InvalidTypeException(String.format(
            "verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb, index,
            tokenTypes, item.getClass().getSimpleName(), statement));
      }
    }

    return token;
  }

  private Object getRawParameter(String verb, List<Object> statement, int index,
      Set<TokenType> tokenTypes) throws InvalidRuleException, InvalidTypeException {
    Object item;
    Token token;

    try {
      item = statement.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidRuleException(String.format(
          "verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
          statement.size()));
    }

    if (tokenTypes != null) {
      TokenType itemType = Token.classify(item);

      if (!tokenTypes.contains(itemType)) {
        throw new InvalidTypeException(String.format(
            "verb '%s' requires parameter #%d to have types %s, not %s. statement=%s", verb, index,
            tokenTypes, statement));
      }
    }

    return item;
  }

  private Token getVariable(String verb, List<Object> statement, int index,
      Map<String, Object> namespace) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException {
    Object item;
    Token token;

    try {
      item = statement.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidRuleException(String.format(
          "verb '%s' requires at least %d items but only %d are available.", verb, index + 1,
          statement.size()));
    }

    try {
      token = new Token(item, namespace);
    } catch (Exception e) {
      throw new StatementErrorException(String.format("parameter %d, %s", index, e));
    }

    if (token.storageType != TokenStorageType.VARIABLE) {
      throw new InvalidTypeException(String.format(
          "verb '%s' requires parameter #%d to be a variable not %s. statement=%s", verb, index,
          token.storageType, statement));
    }

    return token;
  }

  public Map<String, Object> process(String assertionJson) throws InvalidRuleException,
      UndefinedValueException, InvalidTypeException {
    ProcessResult result;
    IdpJson json = new IdpJson();
    Map<String, Object> assertion = (Map<String, Object>) json.loadJson(assertionJson);
    System.out.println(assertionJson);
    System.out.println(json.dumpJson(assertion));
    this.success = true;

    for (int ruleNumber = 0; ruleNumber < this.rules.size(); ruleNumber++) {
      Map<String, Object> namespace = new HashMap<String, Object>();
      Map<String, Object> rule = (Map<String, Object>) this.rules.get(ruleNumber);
      namespace.put(RULE_NUMBER, new Long(ruleNumber));
      namespace.put(RULE_NAME, new String(""));
      namespace.put(ASSERTION, deepCopy(assertion));

      result = processRule(namespace, rule);

      if (result == ProcessResult.RULE_SUCCESS) {
        Map<String, Object> mapped = new LinkedHashMap<String, Object>();
        Map<String, Object> mapping = getMapping(namespace, rule);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) mapping).entrySet()) {
          String key = entry.getKey();
          Object value = entry.getValue();
          Object newValue = null;
          try {
            Token token = new Token(value, namespace);
            newValue = token.get();
          } catch (Exception e) {
            throw new InvalidRuleException(String.format(
                "%s unable to get value for mapping %s=%s, %s", ruleId(namespace), key, value, e),
                e);
          }
          mapped.put(key, newValue);
        }
        return mapped;
      }
    }
    return null;
  }

  private ProcessResult processRule(Map<String, Object> namespace, Map<String, Object> rule)
      throws InvalidRuleException, UndefinedValueException, InvalidTypeException

  {
    ProcessResult result = ProcessResult.BLOCK_CONTINUE;
    List<List> statementBlocks = (List<List>) rule.get("statement_blocks");
    if (statementBlocks == null) {
      throw new InvalidRuleException("rule missing 'statement_blocks'");

    }
    for (int blockNumber = 0; blockNumber < statementBlocks.size(); blockNumber++) {
      List<List> block = (List<List>) statementBlocks.get(blockNumber);
      namespace.put(BLOCK_NUMBER, new Long(blockNumber));
      namespace.put(BLOCK_NAME, "");

      result = processBlock(namespace, block);
      System.out.println();
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

  private ProcessResult processBlock(Map<String, Object> namespace, List<List> statements)
      throws InvalidRuleException, UndefinedValueException, InvalidTypeException {
    ProcessResult result = ProcessResult.STATEMENT_CONTINUE;

    for (int statementNumber = 0; statementNumber < statements.size(); statementNumber++) {
      List<Object> statement = (List<Object>) statements.get(statementNumber);
      namespace.put(STATEMENT_NUMBER, new Long(statementNumber));

      try {
        result = processStatement(namespace, statement);
      } catch (Exception e) {
        throw new IllegalStateException(String.format("%s statement=%s %s", statementId(namespace),
            statement, e), e);
      }
      if (EnumSet.of(ProcessResult.BLOCK_CONTINUE, ProcessResult.RULE_SUCCESS,
          ProcessResult.RULE_FAIL).contains(result)) {
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

  private ProcessResult processStatement(Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException, InvalidValueException {
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

  private ProcessResult verbSet(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token parameter = getParameter(verb, statement, 2, namespace, null);

    variable.set(parameter.value);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s", statementId(namespace),
          verb, this.success, variable, variable.get()));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbLength(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token parameter =
        getParameter(verb, statement, 2, namespace,
            EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));
    long length;


    switch (parameter.type) {
      case ARRAY: {
        length = ((List) parameter.value).size();
      }
        break;
      case MAP: {
        length = ((Map) parameter.value).size();
      }
        break;
      case STRING: {
        length = ((String) parameter.value).length();
      }
        break;
      default:
        throw new IllegalStateException(String.format("unexpected token type: %s", parameter.type));
    }

    variable.set(new Long(length));
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s",
          statementId(namespace), verb, this.success, variable, variable.get(), parameter.value));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbInterpolate(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, InvalidValueException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    String string = (String) getRawParameter(verb, statement, 2, EnumSet.of(TokenType.STRING));
    String newValue = null;

    try {
      newValue = substituteVariables(string, namespace);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, variable='%s' string='%s': %s", verb, variable, string, e));
    }
    variable.set(newValue);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s string='%s'",
          statementId(namespace), verb, this.success, variable, variable.get(), string));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbAppend(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException, InvalidValueException {
    Token variable =
        getToken(verb, statement, 1, namespace, EnumSet.of(TokenStorageType.VARIABLE),
            EnumSet.of(TokenType.ARRAY));
    Token item = getParameter(verb, statement, 2, namespace, null);

    try {
      List<Object> list = (List<Object>) variable.value;
      list.add(item.value);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, variable='%s' item='%s': %s", verb, variable.value, item.value, e));
    }
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s item=%s",
          statementId(namespace), verb, this.success, variable, variable.get(), item.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbUnique(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token array = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.ARRAY));

    List<Object> newValue = new ArrayList<Object>();
    Set<Object> seen = new HashSet<Object>();

    for (Object member : (List<Object>) array.value) {
      if (seen.contains(member)) {
        continue;
      } else {
        newValue.add(member);
        seen.add(member);
      }
    }

    variable.set(newValue);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s array=%s",
          statementId(namespace), verb, this.success, variable, variable.get(), array.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbSplit(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      InvalidValueException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token string = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
    Token pattern = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));

    Pattern regexp;
    List<String> newValue;

    try {
      regexp = Pattern.compile((String) pattern.value);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, bad regular expression pattern '%s', %s", verb, pattern.value, e));
    }
    try {
      newValue = new ArrayList<String>(Arrays.asList(regexp.split((String) string.value)));
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, string='%s' pattern='%s', %s", verb, string.value, pattern.value, e));
    }

    variable.set(newValue);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s string='%s' pattern='%s'",
          statementId(namespace), verb, this.success, variable, variable.get(), string.value,
          pattern.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbJoin(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      InvalidValueException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token array = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.ARRAY));
    Token conjunction = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));
    String newValue;

    try {
      newValue = join((List<Object>) array.value, (String) conjunction.value);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, array=%s conjunction='%s', %s", verb, array.value, conjunction.value,
          e));
    }

    variable.set(newValue);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format(
          "%s verb='%s' success=%s variable: %s=%s array='%s' conjunction='%s'",
          statementId(namespace), verb, this.success, variable, variable.get(), array.value,
          conjunction.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbLower(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidValueException, InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token parameter =
        getParameter(verb, statement, 2, namespace,
            EnumSet.of(TokenType.STRING, TokenType.ARRAY, TokenType.MAP));

    try {
      switch (parameter.type) {
        case STRING: {
          String oldValue = (String) parameter.value;
          String newValue;
          newValue = oldValue.toLowerCase();
          variable.set(newValue);
        }
          break;
        case ARRAY: {
          List<Object> oldValue = (List) parameter.value;
          List<Object> newValue = new ArrayList(oldValue.size());
          String oldItem;
          String newItem;

          for (Object item : oldValue) {
            try {
              oldItem = (String) item;
            } catch (ClassCastException e) {
              throw new InvalidValueException(String.format(
                  "verb '%s' failed, array item (%s) is not a string, array=%s", verb, item,
                  parameter.value));
            }
            newItem = oldItem.toLowerCase();
            newValue.add(newItem);
          }
          variable.set(newValue);
        }
          break;
        case MAP: {
          Map<String, Object> oldValue = (Map<String, Object>) parameter.value;
          Map<String, Object> newValue = new LinkedHashMap<String, Object>(oldValue.size());

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
          throw new IllegalStateException(
              String.format("unexpected token type: %s", parameter.type));
      }
    } catch (Exception e) {
      throw new InvalidValueException(
          String.format("verb '%s' failed, variable='%s' parameter='%s': %s", verb, variable,
              parameter.value, e), e);
    }
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s",
          statementId(namespace), verb, this.success, variable, variable.get(), parameter.value));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbUpper(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidValueException, InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token parameter =
        getParameter(verb, statement, 2, namespace,
            EnumSet.of(TokenType.STRING, TokenType.ARRAY, TokenType.MAP));

    try {
      switch (parameter.type) {
        case STRING: {
          String oldValue = (String) parameter.value;
          String newValue;
          newValue = oldValue.toUpperCase();
          variable.set(newValue);
        }
          break;
        case ARRAY: {
          List<Object> oldValue = (List) parameter.value;
          List<Object> newValue = new ArrayList(oldValue.size());
          String oldItem;
          String newItem;

          for (Object item : oldValue) {
            try {
              oldItem = (String) item;
            } catch (ClassCastException e) {
              throw new InvalidValueException(String.format(
                  "verb '%s' failed, array item (%s) is not a string, array=%s", verb, item,
                  parameter.value));
            }
            newItem = oldItem.toUpperCase();
            newValue.add(newItem);
          }
          variable.set(newValue);
        }
          break;
        case MAP: {
          Map<String, Object> oldValue = (Map<String, Object>) parameter.value;
          Map<String, Object> newValue = new LinkedHashMap<String, Object>(oldValue.size());

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
          throw new IllegalStateException(
              String.format("unexpected token type: %s", parameter.type));
      }
    } catch (Exception e) {
      throw new InvalidValueException(
          String.format("verb '%s' failed, variable='%s' parameter='%s': %s", verb, variable,
              parameter.value, e), e);
    }
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s variable: %s=%s parameter=%s",
          statementId(namespace), verb, this.success, variable, variable.get(), parameter.value));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbIn(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException {
    Token member = getParameter(verb, statement, 1, namespace, null);
    Token collection =
        getParameter(verb, statement, 2, namespace,
            EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));

    switch (collection.type) {
      case ARRAY: {
        this.success = ((List) collection.value).contains(member.value);
      }
        break;
      case MAP: {
        if (member.type != TokenType.STRING) {
          throw new InvalidTypeException(String.format(
              "verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
              TokenType.STRING, collection.type));
        }
        this.success = ((Map) collection.value).containsKey(member.value);
      }
        break;
      case STRING: {
        if (member.type != TokenType.STRING) {
          throw new InvalidTypeException(String.format(
              "verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
              TokenType.STRING, collection.type));
        }
        this.success = ((String) collection.value).contains((String) member.value);
      }
        break;
      default:
        throw new IllegalStateException(String.format("unexpected token type: %s", collection.type));
    }


    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s member=%s collection=%s",
          statementId(namespace), verb, this.success, member.value, collection.value));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbNotIn(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException {
    Token member = getParameter(verb, statement, 1, namespace, null);
    Token collection =
        getParameter(verb, statement, 2, namespace,
            EnumSet.of(TokenType.ARRAY, TokenType.MAP, TokenType.STRING));

    switch (collection.type) {
      case ARRAY: {
        this.success = !((List) collection.value).contains(member.value);
      }
        break;
      case MAP: {
        if (member.type != TokenType.STRING) {
          throw new InvalidTypeException(String.format(
              "verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
              TokenType.STRING, collection.type));
        }
        this.success = !((Map) collection.value).containsKey(member.value);
      }
        break;
      case STRING: {
        if (member.type != TokenType.STRING) {
          throw new InvalidTypeException(String.format(
              "verb '%s' requires parameter #1 to be a %swhen parameter #2 is a %s",
              TokenType.STRING, collection.type));
        }
        this.success = !((String) collection.value).contains((String) member.value);
      }
        break;
      default:
        throw new IllegalStateException(String.format("unexpected token type: %s", collection.type));
    }


    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s member=%s collection=%s",
          statementId(namespace), verb, this.success, member.value, collection.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbCompare(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    Token left = getParameter(verb, statement, 1, namespace, null);
    Token op = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
    Token right = getParameter(verb, statement, 3, namespace, null);
    String invalidOp = "operator %s not supported for type %s";
    TokenType tokenType;
    String opValue = (String) op.value;
    boolean result;

    if (left.type != right.type) {
      throw new InvalidTypeException(String.format(
          "verb '%s' both items must have the same type left is %s and right is %s", verb,
          left.type, right.type));
    } else {
      tokenType = left.type;
    }

    switch (opValue) {
      case "==":
      case "!=": {
        switch (tokenType) {
          case STRING: {
            String leftValue = (String) left.value;
            String rightValue = (String) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case INTEGER: {
            Long leftValue = (Long) left.value;
            Long rightValue = (Long) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case REAL: {
            Double leftValue = (Double) left.value;
            Double rightValue = (Double) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case ARRAY: {
            List<Object> leftValue = (List<Object>) left.value;
            List<Object> rightValue = (List<Object>) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case MAP: {
            Map<String, Object> leftValue = (Map<String, Object>) left.value;
            Map<String, Object> rightValue = (Map<String, Object>) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case BOOLEAN: {
            Boolean leftValue = (Boolean) left.value;
            Boolean rightValue = (Boolean) right.value;
            result = leftValue.equals(rightValue);
          }
            break;
          case NULL: {
            result = (left.value == right.value);
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
            String leftValue = (String) left.value;
            String rightValue = (String) right.value;
            result = leftValue.compareTo(rightValue) < 0;
          }
            break;
          case INTEGER: {
            Long leftValue = (Long) left.value;
            Long rightValue = (Long) right.value;
            result = leftValue < rightValue;
          }
            break;
          case REAL: {
            Double leftValue = (Double) left.value;
            Double rightValue = (Double) right.value;
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
            String leftValue = (String) left.value;
            String rightValue = (String) right.value;
            result = leftValue.compareTo(rightValue) > 0;
          }
            break;
          case INTEGER: {
            Long leftValue = (Long) left.value;
            Long rightValue = (Long) right.value;
            result = leftValue > rightValue;
          }
            break;
          case REAL: {
            Double leftValue = (Double) left.value;
            Double rightValue = (Double) right.value;
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
        throw new InvalidRuleException(String.format(
            "verb '%s' has unknown comparison operator '%s'", verb, op.value));
      }
    }
    this.success = result;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s left=%s op='%s' right=%s",
          statementId(namespace), verb, this.success, left.value, op.value, right.value));
    }
    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbRegexp(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException, InvalidValueException {
    Token string = getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
    Token pattern = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));

    Pattern regexp;
    Matcher matcher;

    try {
      regexp = Pattern.compile((String) pattern.value);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, bad regular expression pattern '%s', %s", verb, pattern.value, e));
    }
    matcher = regexp.matcher((String) string.value);

    if (matcher.find()) {
      this.success = true;
      namespace.put(REGEXP_ARRAY_VARIABLE, regexpGroupList(matcher));
      namespace.put(REGEXP_MAP_VARIABLE, regexpGroupMap((String) pattern.value, matcher));
    } else {
      this.success = false;
      namespace.put(REGEXP_ARRAY_VARIABLE, new ArrayList<Object>());
      namespace.put(REGEXP_MAP_VARIABLE, new HashMap<String, Object>());
    }


    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s string='%s' pattern='%s' %s=%s %s=%s",
          statementId(namespace), verb, this.success, string.value, pattern.value,
          REGEXP_ARRAY_VARIABLE, namespace.get(REGEXP_ARRAY_VARIABLE), REGEXP_MAP_VARIABLE,
          namespace.get(REGEXP_MAP_VARIABLE)));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbRegexpReplace(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException, InvalidValueException {
    Token variable = getVariable(verb, statement, 1, namespace);
    Token string = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
    Token pattern = getParameter(verb, statement, 3, namespace, EnumSet.of(TokenType.STRING));
    Token replacement = getParameter(verb, statement, 4, namespace, EnumSet.of(TokenType.STRING));

    Pattern regexp;
    Matcher matcher;
    String newValue;

    try {
      regexp = Pattern.compile((String) pattern.value);
    } catch (Exception e) {
      throw new InvalidValueException(String.format(
          "verb '%s' failed, bad regular expression pattern '%s', %s", verb, pattern.value, e));
    }
    matcher = regexp.matcher((String) string.value);

    newValue = matcher.replaceAll((String) replacement.value);
    variable.set(newValue);
    this.success = true;

    if (logger.isDebugEnabled()) {
      logger.debug(String.format(
          "%s verb='%s' success=%s variable: %s=%s string='%s' pattern='%s' replacement='%s'",
          statementId(namespace), verb, this.success, variable, variable.get(), string.value,
          pattern.value, replacement.value));
    }

    return ProcessResult.STATEMENT_CONTINUE;
  }

  private ProcessResult verbExit(String verb, Map<String, Object> namespace, List<Object> statement)
      throws InvalidRuleException, StatementErrorException, InvalidTypeException,
      UndefinedValueException {
    ProcessResult statementResult = ProcessResult.STATEMENT_CONTINUE;

    Token exitStatusParam =
        getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
    Token criteriaParam = getParameter(verb, statement, 2, namespace, EnumSet.of(TokenType.STRING));
    String exitStatus = ((String) exitStatusParam.value).toLowerCase();
    String criteria = ((String) criteriaParam.value).toLowerCase();
    ProcessResult result;
    boolean doExit;


    if (exitStatus.equals("rule_succeeds")) {
      result = ProcessResult.RULE_SUCCESS;
    } else if (exitStatus.equals("rule_fails")) {
      result = ProcessResult.RULE_FAIL;
    } else {
      throw new InvalidRuleException(String.format("verb='%s' unknown exit status '%s'", verb,
          exitStatus));
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
      throw new InvalidRuleException(String.format("verb='%s' unknown exit criteria '%s'", verb,
          criteria));
    }

    if (doExit) {
      statementResult = result;
    }

    if (logger.isDebugEnabled()) {
      logger.debug(String
          .format("%s verb='%s' success=%s status=%s criteria=%s exiting=%s result=%s",
              statementId(namespace), verb, this.success, exitStatus, criteria, doExit,
              statementResult));
    }

    return statementResult;
  }

  private ProcessResult verbContinue(String verb, Map<String, Object> namespace,
      List<Object> statement) throws InvalidRuleException, StatementErrorException,
      InvalidTypeException, UndefinedValueException {
    ProcessResult statementResult = ProcessResult.STATEMENT_CONTINUE;
    Token criteriaParam = getParameter(verb, statement, 1, namespace, EnumSet.of(TokenType.STRING));
    String criteria = ((String) criteriaParam.value).toLowerCase();
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
      throw new InvalidRuleException(String.format("verb='%s' unknown continue criteria '%s'",
          verb, criteria));
    }

    if (doContinue) {
      statementResult = ProcessResult.BLOCK_CONTINUE;
    }

    if (logger.isDebugEnabled()) {
      logger.debug(String.format("%s verb='%s' success=%s criteria=%s continuing=%s result=%s",
          statementId(namespace), verb, this.success, criteria, doContinue, statementResult));
    }

    return statementResult;
  }

}
