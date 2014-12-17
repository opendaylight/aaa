Cassandra stores data in the following format:
Map<RowKey, SortedMap<ColumnKey, ColumnValue>>

Row keys are not sorted if Random Partitioner is used. Assume they are not sorted. To implement sorting use wide rows.

About CQL (Cassandra Query Language):
Cassandra Query Language (CQL) is based on SQL (Structured Query Language), the standard for relational 
database manipulation. Although CQL has many similarities to SQL, there are some fundamental differences. 
For example, CQL is adapted to the Cassandra data model and architecture so there is still no allowance 
for SQL-like operations such as JOINs or range queries over rows on clusters that use the random partitioner. 
CQL is mainly used by the database administrator to execute queries via the console (Cassandra CQL Shell).
CQL should not be used by applications. CQL is not like SQL and has its limitations. Everything that can 
be achieved via CQL can be achieved using the Cassandra client (Astyanax for example) in a type-safe manner.

About secondary indexes:

Using a secondary index makes Cassandra create an extra column family
to group rows together by the secondary index. In the extra table the row key is the column value for which
the secondary index was created. For example, if a seconday index was created for an enumeration column, 
each enumeration constant would become a row key and each row will contain all records having the constant 
value. If a column storing a natural key (Like SSN for persons or Serial number for devices) is indexed then 
Cassandra will create a column family having the same number of rows since the indexed column has unique values.
If we don't use secondary indexes we need to create the extra column families manually. Example of secondary index:

---------------------------------------------------------------------------------------
Column family Person
---------------------------------------------------------------------------------------
<Row Key, 000000001>  <Birthdate, 1980-01-01>  <Name, Jonh>    <Status, Single>     
<Row Key, 000000002>  <Birthdate, 1981-01-01>  <Name, Paul>    <Status, Single>
<Row Key, 000000003>  <Birthdate, 1982-01-01>  <Name, Jack>    <Status, Divorce>
<Row Key, 000000004>  <Birthdate, 1980-01-01>  <Name, Steven>  <Status, Married>
---------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------
"Status" Secondary Index (Internal column family handled by Cassandra).
---------------------------------------------------------------------------------------
<Row Key, Single>    <000000001, <Birthdate, 1980-01-01>, <Name, Jonh>>   <000000002, <Birthdate, 1981-01-01>, <Name, Paul>>     
<Row Key, Divorced>  <000000003, <Birthdate, 1982-01-01>, <Name, Jack>>
<Row Key, Married>   <000000004, <Birthdate, 1980-01-01>, <Name, Steven>>
---------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------
"Name" Secondary Index (Internal column family handled by Cassandra).
---------------------------------------------------------------------------------------
<Row Key, Jonh>    <000000001, <Birthdate, 1980-01-01>, <Steven, Single>>     
<Row Key, Paul>    <000000002, <Birthdate, 1981-01-01>, <Steven, Single>>
<Row Key, Jack>    <000000003, <Birthdate, 1982-01-01>, <Steven, Divorce>>
<Row Key, Steven>  <000000004, <Birthdate, 1980-01-01>, <Steven, Married>>
---------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------
Manual Column Family to filter by year of birth (This table just keeps the person key)
Column name is the person key and columns have no value.
---------------------------------------------------------------------------------------
<Row Key, 1980>    <000000001, null>   <000000004, null>     
<Row Key, 1981>    <000000002, null>
<Row Key, 1982>    <000000003, null>
---------------------------------------------------------------------------------------


To handle timestamps order or filtering records can be grouped by periods. For example
creating a column family where the row key is the day and the columns are all records having a timestamp that
belongs to the day (Or week, month, year etc).

Super-columns:
Any request for a sub-column deserializes all sub-columns for that super column, so you should avoid data 
models that rely on on large numbers of sub-columns. Current Cassandra only support one level of super 
column (Path depth of 2).
A composite column is also used to implement super columns. Astyanax dropped super columns support in favor of 
composite columns. Cassandra will eventually replace the super column implementation with composite columns.

A FEW POINTS:

See Cassandra Data Modeling best practices at 
http://www.ebaytechblog.com/2012/07/16/cassandra-data-modeling-best-practices-part-1/
 
You can't sort big data. That's one of the fundamental assumptions. The only things that you can sort by on 
cassandra, are the things that cassandra uses to store its data - the row key (depending on the partition 
strategy but normally assume it is not possible) and the column key. Moving to NoSQL from normal SQL you have 
to drop the notion of being able to sort/join data. It's just (generally) not possible in Big Data implementations.

A database shard is a horizontal partition in a database or search engine. Each individual partition is referred
 to as a shard or database shard. Row sharding in Cassandra is used when a row could grow too big. For example
 in time series, if the column key is the timestamp, we could use the day as sharding and store all records
 of the same day in the same row. See an example in http://rubyscale.com/blog/2011/03/06/basic-time-series-with-cassandra/

See an example of pagination in http://apmblog.compuware.com/2011/12/05/pagination-with-cassandra-and-what-we-can-learn-from-it/
