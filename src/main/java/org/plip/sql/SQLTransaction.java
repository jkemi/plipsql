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
import java.sql.SQLException;

/**
 * try-with-resource compatible wrapper for transactions
 *
 * Example usage:
 *
 * <pre><code>
 *  try (SQLTransaction trans = new SQLTransaction(connection)) {
 * 	    ... do stuff with messages and such
 *      trans.commit()
 *  }
 * </code></pre>
 *
 * This ensures that {@link Connection#rollback()} is called in all cases where {@link #commit()} isn't reached.
 * @since 1.0
 */
public class SQLTransaction implements SQLClosable {
	private Connection connection;

	public SQLTransaction(Connection connection) throws SQLException {
		if (connection == null) {
			throw new NullPointerException("connection mustn't be null");
		}
		if (connection.getAutoCommit()) {
			throw new SQLException("connection must not be in auto-commit mode");
		}

		this.connection = connection;
	}

	/**
	 * Commits any changes in this transaction.
	 *
	 *
	 * @throws java.sql.SQLException
	 * @throws java.lang.IllegalStateException
	 *
	 * @see Connection#commit()
	 */
	public void commit() throws SQLException, IllegalStateException {
		if (connection == null) {
			throw new IllegalStateException("not in transaction");
		}
		try {
			connection.commit();
		} finally {
			connection = null;
		}
	}

	/**
	 * Performs an explicit rollback.
	 *
	 *
	 * It should not be necessary to call this method explicitly as
	 * as it's taken care of in {@link #close()}
	 * However it is still safe to call this method, in which case nothing is performed by {@link #close()}.
	 *
	 * @throws java.sql.SQLException if rollback fails
	 * @throws java.lang.IllegalStateException if already committed or rolled back
	 *
	 * @see Connection#rollback()
	 */
	public void rollback() throws SQLException, IllegalStateException {
		if (connection == null) {
			throw new IllegalStateException("not in transaction");
		}
		try {
			connection.rollback();
		} finally {
			connection = null;
		}
	}

	/**
	 * Rolls back any uncommitted session state, or if {@link #rollback()} or {@link #commit()} has already been called
	 * performs nothing.
	 *
	 * @throws java.sql.SQLException if rollback fails
	 *
	 * @see #rollback()
	 */
	@Override
	public void close() throws SQLException {
		if (connection != null) {
			try {
				connection.rollback();
			} finally {
				connection = null;
			}
		}
	}

}
