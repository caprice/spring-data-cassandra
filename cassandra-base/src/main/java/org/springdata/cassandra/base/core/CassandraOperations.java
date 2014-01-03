/*
 * Copyright 2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springdata.cassandra.base.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.springdata.cassandra.base.core.query.ExecuteOptions;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * Operations for interacting with Cassandra at the lowest level. This interface provides Exception Translation.
 * 
 * @author David Webb
 * @author Matthew Adams
 * @author Alex Shvid
 */
public interface CassandraOperations {

	/**
	 * Executes the supplied {@link SessionCallback} in the current Template Session. The implementation of
	 * SessionCallback can decide whether or not to <code>execute()</code> or <code>executeAsync()</code> the operation.
	 * 
	 * @param sessionCallback
	 * @return Type<T> defined in the SessionCallback
	 */
	<T> T execute(SessionCallback<T> sessionCallback);

	/**
	 * Executes the supplied CQL Query and returns nothing.
	 * 
	 * @param asynchronously Flag to execute asynchronously
	 * @param cql The Query
	 * @param optionsOrNull Execute Options Object if exists
	 */
	void execute(boolean asynchronously, String cql, ExecuteOptions optionsOrNull);

	/**
	 * Executes the provided CQL Query, and extracts the results with the ResultSetCallback.
	 * 
	 * @param cql The Query
	 * @param rsc The implementation for extracting data from the ResultSet
	 * @param optionsOrNull Execute Options Object if exists
	 * 
	 * @return
	 */
	<T> T select(String cql, ResultSetCallback<T> rsc, ExecuteOptions optionsOrNull);

	/**
	 * Executes the provided CQL Query, and extracts the results with the ResultSetCallback with given timeout.
	 * 
	 * @param cql The Query
	 * @param rsc The implementation for extracting data from the ResultSet
	 * @param timeoutMls Nonstop timeout in milliseconds
	 * @param optionsOrNull Execute Options Object if exists
	 * @return extracted value T or TimeoutException
	 */
	<T> T selectNonstop(String cql, ResultSetCallback<T> rsc, int timeoutMls, ExecuteOptions optionsOrNull)
			throws TimeoutException;

	/**
	 * Executes the provided CQL Query asynchronously, and extracts the results with the ResultSetFutureExtractor
	 * 
	 * @param cql The Query
	 * @param optionsOrNull Execute Options Object if exists
	 * @return CassandraFuture<ResultSet>
	 */
	CassandraFuture<ResultSet> selectAsync(String cql, ExecuteOptions optionsOrNull);

	/**
	 * Executes the provided CQL Query, and then processes the results with the <code>RowCallbackHandler</code>.
	 * 
	 * @param cql The Query
	 * @param rch The implementation for processing the rows returned.
	 * @param optionsOrNull Query Options Object
	 */
	void select(String cql, RowCallbackHandler rch, ExecuteOptions optionsOrNull);

	/**
	 * Executes the provided CQL Query, and then processes the results with the <code>AsyncRowCallbackHandler</code>.
	 * 
	 * @param cql The Query
	 * @param rch The implementation for processing the rows returned.
	 * @param executor Thread executor for asynchronous request
	 * @param optionsOrNull Query Options Object
	 */

	void selectAsync(String cql, RowCallbackHandler.Async rch, Executor executor, ExecuteOptions optionsOrNull);

	/**
	 * Processes the ResultSet through the RowCallbackHandler and return nothing. This is used internal to the Template
	 * for core operations, but is made available through Operations in the event you have a ResultSet to process. The
	 * ResultsSet could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet Results to process
	 * @param rch RowCallbackHandler with the processing implementation
	 */
	void process(ResultSet resultSet, RowCallbackHandler rch);

	/**
	 * Executes the provided CQL Query, and maps all Rows returned with the supplied RowMapper.
	 * 
	 * @param cql The Query
	 * @param rowMapper The implementation for mapping all rows
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Iterator of <T> processed by the RowMapper
	 */
	<T> Iterator<T> select(String cql, RowMapper<T> rowMapper, ExecuteOptions optionsOrNull);

	/**
	 * 
	 * @param cql The Query
	 * @param rowMapper The implementation for mapping all rows
	 * @param optionsOrNull Execute Options Object if exists
	 * @return CassandraFuture<Iterator<T>> The future of the Iterator of <T> processed by the RowMapper
	 */

	<T> CassandraFuture<Iterator<T>> selectAsync(String cql, RowMapper<T> rowMapper, ExecuteOptions optionsOrNull);

	/**
	 * Processes the ResultSet through the RowMapper and returns the List of mapped Rows. This is used internal to the
	 * Template for core operations, but is made available through Operations in the event you have a ResultSet to
	 * process. The ResultsSet could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet Results to process
	 * @param rowMapper RowMapper with the processing implementation
	 * @return Iterator of <T> generated by the RowMapper
	 */
	<T> Iterator<T> process(ResultSet resultSet, RowMapper<T> rowMapper);

	/**
	 * Executes the provided CQL Query, and maps <b>ONE</b> Row returned with the supplied RowMapper.
	 * 
	 * <p>
	 * This expects only ONE row to be returned. More than one Row will cause an Exception to be thrown.
	 * </p>
	 * 
	 * @param cql The Query
	 * @param rowMapper The implementation for convert the Row to <T>
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Object<T>
	 */
	<T> T selectOne(String cql, RowMapper<T> rowMapper, ExecuteOptions optionsOrNull);

	/**
	 * Process a ResultSet through a RowMapper. This is used internal to the Template for core operations, but is made
	 * available through Operations in the event you have a ResultSet to process. The ResultsSet could come from a
	 * ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet
	 * @param rowMapper
	 * @return
	 */
	<T> T processOne(ResultSet resultSet, RowMapper<T> rowMapper);

	/**
	 * Executes the provided query and tries to return the first column of the first Row as a Class<T>.
	 * 
	 * @param cql The Query
	 * @param elementType Valid Class that Cassandra Data Types can be converted to.
	 * @param optionsOrNull Execute Options Object if exists
	 * @return The Object<T> - item [0,0] in the result table of the query.
	 */
	<T> T selectOne(String cql, Class<T> elementType, ExecuteOptions optionsOrNull);

	/**
	 * Process a ResultSet, trying to convert the first columns of the first Row to Class<T>. This is used internal to the
	 * Template for core operations, but is made available through Operations in the event you have a ResultSet to
	 * process. The ResultsSet could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet
	 * @param elementType
	 * @return
	 */
	<T> T processOne(ResultSet resultSet, Class<T> elementType);

	/**
	 * Executes the provided CQL Query and maps <b>ONE</b> Row to a basic Map of Strings and Objects. If more than one Row
	 * is returned from the Query, an exception will be thrown.
	 * 
	 * @param cql The Query
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Map representing the results of the Query
	 */
	Map<String, Object> selectOneAsMap(String cql, ExecuteOptions optionsOrNull);

	/**
	 * Process a ResultSet with <b>ONE</b> Row and convert to a Map. This is used internal to the Template for core
	 * operations, but is made available through Operations in the event you have a ResultSet to process. The ResultsSet
	 * could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet
	 * @return
	 */
	Map<String, Object> processOneAsMap(ResultSet resultSet);

	/**
	 * Executes the provided CQL and returns all values in the first column of the Results as a List of the Type in the
	 * second argument.
	 * 
	 * @param cql The Query
	 * @param elementType Type to cast the data values to
	 * @param optionsOrNull Execute Options Object if exists
	 * @return List of elementType
	 */
	<T> List<T> selectFirstColumnAsList(String cql, Class<T> elementType, ExecuteOptions optionsOrNull);

	/**
	 * Process a ResultSet and convert the first column of the results to a List. This is used internal to the Template
	 * for core operations, but is made available through Operations in the event you have a ResultSet to process. The
	 * ResultsSet could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet
	 * @param elementType
	 * @return
	 */
	<T> List<T> processFirstColumnAsList(ResultSet resultSet, Class<T> elementType);

	/**
	 * Executes the provided CQL and converts the results to a basic List of Maps. Each element in the List represents a
	 * Row returned from the Query. Each Row's columns are put into the map as column/value.
	 * 
	 * @param cql The Query
	 * @param optionsOrNull Execute Options Object if exists
	 * @return List of Maps with the query results
	 */
	List<Map<String, Object>> selectAsListOfMap(String cql, ExecuteOptions optionsOrNull);

	/**
	 * Process a ResultSet and convert it to a List of Maps with column/value. This is used internal to the Template for
	 * core operations, but is made available through Operations in the event you have a ResultSet to process. The
	 * ResultsSet could come from a ResultSetFuture after an asynchronous query.
	 * 
	 * @param resultSet
	 * @return
	 */
	List<Map<String, Object>> processAsListOfMap(ResultSet resultSet);

	/**
	 * Converts the CQL provided into a {@link SimplePreparedStatementCreator}. <b>This can only be used for CQL
	 * Statements that do not have data binding.</b>
	 * 
	 * @param cql The CQL Statement to Execute
	 * @return PreparedStatement
	 */

	PreparedStatement prepareStatement(String cql, ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new PreparedSession
	 * 
	 * @param psc The implementation to create the PreparedStatement
	 * @return PreparedStatement
	 */

	PreparedStatement prepareStatement(PreparedStatementCreator psc, ExecuteOptions optionsOrNull);

	/**
	 * Executes the prepared statement and processes the statement using the provided Callback. <b>This can only be used
	 * for CQL Statements that do not have data binding.</b> The results of the PreparedStatement are processed with
	 * PreparedStatementCallback implementation provided by the Application Code.
	 * 
	 * @param psc The implementation to create the PreparedStatement
	 * @param psc What to do with the results of the PreparedStatement
	 * @return Type<T> as determined by the supplied Callback.
	 */
	<T> T execute(PreparedStatement ps, PreparedStatementCallback<T> psc);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. <b>This can only be used for CQL
	 * Statements that do not have data binding.</b> The results of the PreparedStatement are processed with
	 * ResultSetCallback implementation provided by the Application Code.
	 * 
	 * @param psc The PreparedStatement
	 * @param rsc Implementation for extracting data from the ResultSet
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Type <T> which is the output of the ResultSetCallback
	 */
	<T> T select(PreparedStatement ps, ResultSetCallback<T> rsc, ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. <b>This can only be used for CQL
	 * Statements that do not have data binding.</b> The results of the PreparedStatement are processed with
	 * RowCallbackHandler and nothing is returned.
	 * 
	 * @param ps The PreparedStatement
	 * @param rch The implementation to process Results
	 * @param optionsOrNull Execute Options Object if exists
	 */
	void select(PreparedStatement ps, RowCallbackHandler rch, ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. <b>This can only be used for CQL
	 * Statements that do not have data binding.</b> The results of the PreparedStatement are processed with RowMapper
	 * implementation provided and a List is returned with elements of Type <T> for each Row returned.
	 * 
	 * @param ps The PreparedStatement
	 * @param rowMapper The implementation for mapping each Row returned.
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Iterator of Type <T> mapped from each Row in the Results
	 */
	<T> Iterator<T> select(PreparedStatement ps, RowMapper<T> rowMapper, ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. Binds the values from the
	 * PreparedStatementBinder to the available bind variables. The results of the PreparedStatement are processed with
	 * ResultSetCallback implementation provided by the Application Code.
	 * 
	 * @param ps The PreparedStatement
	 * @param psbOrNull The implementation to bind variables to values if exists
	 * @param rsc Implementation for extracting data from the ResultSet
	 * @param optionsOrNull Execute Options Object if exists
	 * @return Type <T> which is the output of the ResultSetCallback
	 */
	<T> T select(PreparedStatement ps, PreparedStatementBinder psbOrNull, ResultSetCallback<T> rsc,
			ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. Binds the values from the
	 * PreparedStatementBinder to the available bind variables. The results of the PreparedStatement are processed with
	 * RowCallbackHandler and nothing is returned.
	 * 
	 * @param ps The PreparedStatement
	 * @param psbOrNull The implementation to bind variables to values if exists
	 * @param rch The implementation to process Results
	 * @param optionsOrNull The Query Options Object if exists
	 * @return Type <T> which is the output of the ResultSetCallback
	 */
	void select(PreparedStatement ps, PreparedStatementBinder psbOrNull, RowCallbackHandler rch,
			ExecuteOptions optionsOrNull);

	/**
	 * Uses the provided PreparedStatementCreator to prepare a new Session call. Binds the values from the
	 * PreparedStatementBinder to the available bind variables. The results of the PreparedStatement are processed with
	 * RowMapper implementation provided and a List is returned with elements of Type <T> for each Row returned.
	 * 
	 * @param ps The PreparedStatement
	 * @param psbOrNull The implementation to bind variables to values if exists
	 * @param rowMapper The implementation for mapping each Row returned.
	 * @param optionsOrNull The Query Options Object if exists
	 * @return Iterator <T> which is the output of the ResultSetCallback
	 */
	<T> Iterator<T> select(PreparedStatement ps, PreparedStatementBinder psbOrNull, RowMapper<T> rowMapper,
			ExecuteOptions optionsOrNull);

	/**
	 * Describe the current Ring. This uses the provided {@link RingMemberHostMapper} to provide the basics of the
	 * Cassandra Ring topology.
	 * 
	 * @return The collection of ring tokens that are active in the cluster
	 */
	Collection<RingMember> describeRing();

	/**
	 * Describe the current Ring. Application code must provide its own {@link HostMapper} implementation to process the
	 * lists of hosts returned by the Cassandra Cluster Metadata.
	 * 
	 * @param hostMapper The implementation to use for host mapping.
	 * @return Collection generated by the provided HostMapper.
	 */
	<T> Collection<T> describeRing(HostMapper<T> hostMapper);

	/**
	 * Get the current Session used for operations in the implementing class.
	 * 
	 * @return The DataStax Driver Session Object
	 */
	Session getSession();

	/**
	 * This is an operation designed for high performance writes. The cql is used to create a PreparedStatement once, then
	 * all row values are bound to the single PreparedStatement and executed against the Session.
	 * 
	 * <p>
	 * This is used internally by the other ingest() methods, but can be used if you want to write your own RowIterator.
	 * The Object[] length returned by the next() implementation must match the number of bind variables in the CQL.
	 * </p>
	 * 
	 * @param cql The CQL
	 * @param rowIterator Implementation to provide the Object[] to be bound to the CQL.
	 * @param optionsOrNull The Query Options Object if exists
	 */
	void ingest(String cql, Iterable<Object[]> rowIterator, ExecuteOptions optionsOrNull);

	/**
	 * This is an operation designed for high performance writes. The cql is used to create a PreparedStatement once, then
	 * all row values are bound to the single PreparedStatement and executed against the Session.
	 * 
	 * <p>
	 * The Object[] length of the nested array must match the number of bind variables in the CQL.
	 * </p>
	 * 
	 * @param cql The CQL
	 * @param rows Object array of Object array of values to bind to the CQL.
	 * @param optionsOrNull The Execute Options Object if exists
	 */
	void ingest(String cql, Object[][] rows, ExecuteOptions optionsOrNull);

	/**
	 * Delete all rows in the table
	 * 
	 * @param asynchronously
	 * @param tableName
	 * @param optionsOrNull
	 */
	void truncate(boolean asynchronously, String tableName, ExecuteOptions optionsOrNull);

	/**
	 * Support keyspace operations
	 * 
	 * @return KeyspaceOperations
	 */

	KeyspaceOperations keyspaceOps();

	/**
	 * Support table operations
	 * 
	 * @param keyspace
	 * @return
	 */

	TableOperations tableOps(String tableName);

	/**
	 * Support index operations
	 * 
	 * @param keyspace
	 * @return
	 */

	IndexOperations indexOps(String tableName);

}