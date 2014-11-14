To compile the CassandraStore.java, you need the following jars imported in your project and the aaa-idmlight project also imported (either the jar or the project itself in eclipse):
cassandra-driver-core-2.1.3-SNAPSHOT.jar
guava-15.0-cdi1.0.jar
metrics-core-3.0.0.jar
netty-3.8.0.Final.jar

If you are using maven, just add the following dependencies to the pom.xml file:
		<dependency>
		  <groupId>com.datastax.cassandra</groupId>
		  <artifactId>cassandra-driver-core</artifactId>
		  <version>2.1.2</version>
		</dependency>    
		<dependency>
		  <groupId>com.google.guava</groupId>
		  <artifactId>guava</artifactId>
		  <version>15.0</version>
		  <classifier>cdi1.0</classifier>
		</dependency>    
		<dependency>
			<groupId>com.codahale.metrics</groupId>
			<artifactId>metrics-core</artifactId>
			<version>3.0.0</version>
		</dependency>    
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty</artifactId>
			<version>3.8.0.Final</version>
		</dependency>    


* Once compiled, use the "export to jar" option in eclipse and export the project to a jar in the location /tmp/jars (It's dump, but i could not get those jars to load in Karaf, to complicated for me... i guess)
* Copy all the above jars to this directory as well
* Copy the store.ini.cassandra in the resources directory to store.ini in the resources directory
* Edit cassandra.ini and place your cassandra node ip address

