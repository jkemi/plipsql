/*
 * This file is part of plipsql Copyright (c) 2010-2015 Jakob Kemi <jakob.kemi@gmail.com>.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A stack of SQLClosable items that are closed in correct order.
 * @since 1.0
 */
public final class SQLStack implements SQLClosable {
//	private static Logger _logger = LoggerFactory.getLogger(SQLStack.class);

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


	/**
	 * Convenience constructor that adds one initial closable to stack
	 * @deprecated Deprecated since 1.1, use {@link #SQLStack()} followed by {@link #push(org.plip.sql.SQLClosable) } instead
	 * @param g
	 */
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
	 * @return this
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
			//_logger.warn("suppressed {} additional exceptions", suppressedCount);
			throw err;
		}
	}

	public void closeSuppress() {
		try {
			close();
		} catch (SQLException e) {
			//_logger.info("suppressed a cascading SQL error on guard close", e);
		}
	}
}
