/*
 * This file is part of plipsql Copyright (c) 2010-2014 Jakob Kemi <jakob.kemi@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.plip.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Wraps a PreparedStatement to provide named parameters.
 *
 * instead of writing statements with question marks, use colon+parameter names (eg. :foo). Multiple occurrence
 * of a parameter is supported.
 *
 * example:
 * <code>
 *   SELECT colA,colB FROM mytable WHERE foo = :foo AND bar > :foo;
 * </code>
 *
 * Initially based on article by Adam Crume (http://www.javaworld.com/article/2077706/core-java/named-parameters-for-preparedstatement.html)
 * Spring's JDBCTemplate is similar also
 *
 * @since 1.1
 */
public class ParameterStatement implements SQLClosable {
	/** The statement this object is wrapping. */
	private final PreparedStatement	statement;
	private final Set<String>		unbound;

	/** Maps parameter names to arrays of ints which are the parameter indices. */
	private final Map<String, List<Integer>> indexMap;

	/**
	 * Creates a NamedParameterStatement. Wraps a call to
	 * {@link Connection#prepareStatement(java.lang.String) prepareStatement}.
	 *
	 * @param connection
	 *            the database connection
	 * @param query
	 *            the parameterized query
	 * @throws SQLException
	 *             if the statement could not be created
	 */
	public ParameterStatement(Connection connection, CharSequence query) throws SQLException {
		indexMap = new HashMap<String, List<Integer>>();
		unbound = new HashSet<String>();
		String parsedQuery = parse(query, indexMap);

		unbound.addAll(indexMap.keySet());

		statement = connection.prepareStatement(parsedQuery);
	}

	public ParameterStatement(Connection connection, String query, Map<String,SQLParameter> sqlParameters) throws SQLException {
		this(connection,query);
		setParameters(sqlParameters);
	}

	/**
	 * Parses a query with named parameters. The parameter-index mappings are
	 * put into the map, and the parsed query is returned.
	 * This method is non-private so JUnit code can test it.
	 *
	 * @param query
	 *				query to parse
	 * @param paramMap
	 *				map to hold parameter-index mappings
	 * @return the parsed query
	 */
	static final String parse(CharSequence query, Map<String, List<Integer>> paramMap) {

		final int length = query.length();
		final StringBuilder parsedQuery = new StringBuilder(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);							// cur char
			char n = (i+1)<length ? query.charAt(i+1) : '\0';	// next char

			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;

				// Chech for casts, starts with double-colon "::..."
				} else if (c == ':' && n == ':') {
					parsedQuery.append(':');
					i++;	// skip one character
					c = ':';

				// Replace :varname with ? and keep varname in mapping
				} else if (c == ':' && Character.isJavaIdentifierStart(n)) {
					int j = i + 2;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}

					CharSequence nameseq = query.subSequence(i+1, j);
					final String name;
					if (nameseq instanceof String) {
						name = (String)nameseq;
					} else {
						name = nameseq.toString();
					}
					c = '?';			// replace the parameter with a question mark
					i = j-1;			// skip past the end if the parameter

					List<Integer> indexList = paramMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList<Integer>();	// slightly smaller than ArrayList for few elements
						paramMap.put(name, indexList);
					}

					indexList.add(index);
					index++;
				}
			}
			parsedQuery.append(c);
		}

		return parsedQuery.toString();
	}

	ParameterStatement setIndices(List<Integer> indices, Object x, int targetSqlType, Integer scaleOrLength) throws SQLException {
		for (int index : indices) {
			if (x == null) {
				statement.setNull(index, targetSqlType);
			} else if (scaleOrLength != null) {
				statement.setObject(index, x, targetSqlType, scaleOrLength);
			} else {
				statement.setObject(index, x, targetSqlType);
			}
		}

		return this;
	}

	/**
	 * See {@link PreparedStatement#setObject(int, java.lang.Object, int)}
	 * See {@link PreparedStatement#setObject(int, java.lang.Object, int, int)}
	 * @param name			parameter name
	 * @param x				parameter value
	 * @param targetSqlType	parameter type (typically one of java.sql.Types.X)
	 * @param scaleOrLength	parameter scale (usually null)
	 * @return this
	 * @throws SQLException
	 */
    public ParameterStatement setObject(String name, Object x, int targetSqlType, Integer scaleOrLength) throws SQLException {
		final List<Integer> indices = indexMap.get(name);
		if (indices == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}

		unbound.remove(name);
		return setIndices(indices, x, targetSqlType, scaleOrLength);
	}

	/**
	 * See {@link PreparedStatement#setObject(int, java.lang.Object, int)}
	 * @param name
	 * @param sqlParameter
	 * @return this
	 * @throws SQLException
	 */
	public ParameterStatement setParameter(String name, SQLParameter sqlParameter) throws SQLException {
		return setObject(name, sqlParameter.value, sqlParameter.sqlType, sqlParameter.scaleOrLength);
	}


	/**
	 * Sets multiple parameters from map
	 * @param namedParameters
	 * @return this
	 * @throws SQLException
	 */
	public final ParameterStatement setParameters(Map<String,SQLParameter> namedParameters) throws SQLException {
		for (Map.Entry<String,List<Integer>> entry : indexMap.entrySet()) {
			final String name = entry.getKey();
			SQLParameter sqlParameter = namedParameters.get(name);
			if (sqlParameter != null) {
				setIndices(entry.getValue(), sqlParameter.value, sqlParameter.sqlType, sqlParameter.scaleOrLength);
				unbound.remove(name);
			}
		}
		return this;
	}

	/**
	 * Returns an unmodifiable set of parameter names
	 * @return parameter names
	 */
	public Set<String> getParameters() {
		return Collections.unmodifiableSet(indexMap.keySet());
	}

	/**
	 * Returns an unmodifiable set of currently unbound parameter names
	 * @return unbound parameter names
	 */
	public Set<String> getUnboundParameters() {
		return Collections.unmodifiableSet(unbound);
	}


	/**
	 * Returns the indices for a parameter.
	 *
	 * @param name
	 *            parameter name
	 * @return parameter indices
	 * @throws IllegalArgumentException
	 *             if the parameter does not exist
	 */
	public List<Integer> getIndices(String name) {
		List<Integer> indices = indexMap.get(name);
		if (indices == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}
		return Collections.unmodifiableList(indices);
	}

	/**
	 * Returns the underlying statement.
	 *
	 * @return the statement
	 */
	public PreparedStatement borrowStatement() {
		return statement;
	}

	/**
	 * Executes the statement.
	 *
	 * @return true if the first result is a {@link ResultSet}
	 * @throws SQLException
	 *             if an error occurred
	 * @see PreparedStatement#execute()
	 */
	public boolean execute() throws SQLException {
		return statement.execute();
	}

	/**
	 * Executes the statement, which must be a query.
	 *
	 * @return the query results
	 * @throws SQLException
	 *             if an error occurred
	 * @see PreparedStatement#executeQuery()
	 */
	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	/**
	 * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
	 * statement; or an SQL statement that returns nothing, such as a DDL
	 * statement.
	 *
	 * @return number of rows affected
	 * @throws SQLException
	 *             if an error occurred
	 * @see PreparedStatement#executeUpdate()
	 */
	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	/**
	 * Closes the statement.
	 *
	 * @throws SQLException
	 *             if an error occurred
	 * @see Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		statement.close();
	}

	/**
	 * Adds the current set of parameters as a batch entry.
	 *
	 * @throws SQLException
	 *             if something went wrong
	 */
	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	/**
	 * Executes all of the batched statements.
	 *
	 * See {@link Statement#executeBatch()} for details.
	 *
	 * @return update counts for each statement
	 * @throws SQLException
	 *             if something went wrong
	 */
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}
}
