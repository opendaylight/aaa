/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

/**
 * Shard strategy to distribute {@link String} type column names into several row keys based on the
 * first char (lower-case converted) of the {@link String} value.
 * <p>
 * Examples:
 * <p>
 * <table border=1>
 * <tr>
 * <th>Prefix</th>
 * <th>Column Name</th>
 * <th>Sharded Row Key</th>
 * </tr>
 * <tr>
 * <td>my_prefix</td>
 * <td>Hello World</td>
 * <td>my_prefix_h</td>
 * </tr>
 * <tr>
 * <td>my_prefix</td>
 * <td>hello world</td>
 * <td>my_prefix_h</td>
 * </tr>
 * <tr>
 * <td>Null</td>
 * <td>Hello World</td>
 * <td>h</td>
 * </tr>
 * <tr>
 * <td>Empty</td>
 * <td>Hello World</td>
 * <td>h</td>
 * </tr>
 * </table>
 * 
 * @author Fabiel Zuniga
 */
public class StringShardStrategy implements ColumnShardStrategy<String, String> {

    private static final String SPECIAL_CHAR_SHARD = "special";

    private final String rowKeyPrefix;

    /**
     * Creates a shard strategy.
     */
    public StringShardStrategy() {
        this(null);
    }

    /**
     * Creates a shard strategy.
     * 
     * @param rowKeyPrefix row key prefix
     */
    public StringShardStrategy(String rowKeyPrefix) {
        this.rowKeyPrefix = rowKeyPrefix != null && !rowKeyPrefix.isEmpty() ? rowKeyPrefix + "_" : "";
    }

    @Override
    public String getShard(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            throw new IllegalArgumentException("columnName cannot be null nor empty");
        }

        String rowKey = null;
        char firstChar = columnName.charAt(0);
        if (Character.isLetterOrDigit(firstChar)) {
            rowKey = this.rowKeyPrefix + String.valueOf(columnName.charAt(0)).toLowerCase();
        }
        else {
            rowKey = this.rowKeyPrefix + SPECIAL_CHAR_SHARD;
        }
        return rowKey;
    }
}
