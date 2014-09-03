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
 * This ensures that {@link Session#rollback()} is called in all cases where {@link #commit()} isn't reached.
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
	 * @see {@link Connection#commit()}
	 *
	 * @throws java.sql.SQLException
	 * @throws java.lang.IllegalStateException
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

	 * @see {@link Connection#rollback()}
	 *
	 * It should not be necessary to call this method explicitly as
	 * as it's taken care of in {@link #close()}
	 * However it is still safe to call this method, in which case nothing is performed by {@link #close()}.
	 *
	 * @throws java.sql.SQLException if rollback fails
	 * @throws java.lang.IllegalStateException if already committed or rolled back
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
	 * @see {@link Session#rollback()}
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
