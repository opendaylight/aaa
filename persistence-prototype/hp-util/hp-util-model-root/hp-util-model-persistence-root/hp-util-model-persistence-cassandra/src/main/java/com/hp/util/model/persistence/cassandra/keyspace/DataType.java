/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

/**
 * Cassandra Data Type.
 * <p>
 * <H3>About Data Types (Comparators and Validators)</H3>
 * <p>
 * In a relational database, you must specify a data type for each column when you define a table.
 * The data type constrains the values that can be inserted into that column. For example, if you
 * have a column defined as an integer datatype, you would not be allowed to insert character data
 * into that column. Column names in a relational database are typically fixed labels (strings) that
 * are assigned when you define the table schema.
 * <p>
 * In Cassandra, the data type for a column (or row key) value is called a validator. The data type
 * for a column name is called a comparator. You can define data types when you create your column
 * family schemas (which is recommended), but Cassandra does not require it. Internally, Cassandra
 * stores column names and values as hex byte arrays (BytesType). This is the default client
 * encoding used if data types are not defined in the column family schema (or if not specified by
 * the client request).
 * <p>
 * Cassandra comes with the following built-in data types, which can be used as both validators (row
 * key and column value data types) or comparators (column name data types). One exception is
 * CounterColumnType, which is only allowed as a column value (not allowed for row keys or column
 * names).
 * <p>
 * <H3>About Validators</H3>
 * <p>
 * For all column families, it is best practice to define a default row key validator using the
 * key_validation_class property.
 * <P>
 * For static column families, you should define each column and its associated type when you define
 * the column family using the column_metadata property.
 * <P>
 * For dynamic column families (where column names are not known ahead of time), you should specify
 * a default_validation_class instead of defining the per-column data types.
 * <P>
 * Key and column validators may be added or changed in a column family definition at any time. If
 * you specify an invalid validator on your column family, client requests that respect that
 * metadata will be confused, and data inserts or updates that do not conform to the specified
 * validator will be rejected.
 * <p>
 * <H3>About Comparators</H3>
 * <p>
 * Within a row, columns are always stored in sorted order by their column name. The comparator
 * specifies the data type for the column name, as well as the sort order in which columns are
 * stored within a row. Unlike validators, the comparator may not be changed after the column family
 * is defined, so this is an important consideration when defining a column family in Cassandra.
 * <P>
 * Typically, static column family names will be strings, and the sort order of columns is not
 * important in that case. For dynamic column families, however, sort order is important. For
 * example, in a column family that stores time series data (the column names are timestamps),
 * having the data in sorted order is required for slicing result sets out of a row of columns.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public interface DataType<D> {

    /**
     * Accepts a command visitor.
     * 
     * @param visitor visitor
     * @param visitorInput visitor's input
     */
    public <I> void accept(DataTypeVisitor<D, I> visitor, I visitorInput);

    /**
     * Accepts a command visitor.
     * 
     * @param visitor visitor
     * @param visitorInput visitor's input
     * @return the command result
     */
    public <E, I> E accept(DataTypeCommandVisitor<D, E, I> visitor, I visitorInput);
}
