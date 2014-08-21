package org.plip.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stack of SQLClosable items that are closed in correct order.
 */
public final class SQLStack implements SQLClosable {

	private static Logger _logger = LoggerFactory.getLogger(SQLStack.class);

	private final Deque<SQLClosable> _deque;

	public SQLStack() {
		_deque = new LinkedBlockingDeque<>();
	}

	private SQLStack(Deque<SQLClosable> d) {
		_deque = new LinkedBlockingDeque<>(d.size());

		// iterate from first to last in deque
		for (SQLClosable c : d) {
			_deque.addLast(c);
		}
	}


	public SQLStack(SQLClosable g) {
		this();
		push(g);
	}

	public <T extends SQLClosable> T push(T g) {
		_deque.addLast(g);
		return g;
	}

	public <T extends Connection> T push(final T db) {
		push(new SQLClosable() {
			@Override
			public void close() throws SQLException {
				db.close();
			}
		});
		return db;
	}
	public <T extends Statement> T push(final T stmt) {
		push(new SQLClosable() {
			@Override
			public void close() throws SQLException {
				stmt.close();
			}
		});
		return stmt;
	}
	public <T extends ResultSet> T push(final T rs) {
		push(new SQLClosable() {
			@Override
			public void close() throws SQLException {
				rs.close();
			}
		});
		return rs;
	}

	public SQLClosable pop() {
		return _deque.pollLast();
	}

	/**
	 * Transfers all elements of this stack into a new instance, making this one empty.
	 * @return
	 */
	public SQLStack claim() {
		SQLStack ret = new SQLStack(_deque);
		_deque.clear();
		return ret;
	}

	@Override
	public void close() throws SQLException {
		SQLException err = null;

		int suppressedCount = 0;

		SQLClosable g;
		while ((g = _deque.pollLast()) != null) {
			try {
				g.close();
			} catch (SQLException e) {
				if (err == null) {
					err = e;
				} else {
					err.setNextException(e);
					err.addSuppressed(e);
					suppressedCount += 1;
				}
			} catch (RuntimeException e) {
				if (err == null) {
					err = new SQLException("caught exception during close", e);
				} else {
					err.addSuppressed(e);
					suppressedCount += 1;
				}
			}
		}

		if (err != null) {
			_logger.warn("suppressed {} additional exceptions", suppressedCount);
			throw err;
		}
	}

	public void closeSuppress() {
		try {
			close();
		} catch (SQLException e) {
			_logger.info("suppressed a cascading SQL error on guard close", e);
		}
	}
}
