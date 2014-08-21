package org.plip.sql;

import java.sql.SQLException;

/**
 * Declares an object that needs to be close():ed but might throw an SQLException.
 */
public interface SQLClosable extends AutoCloseable {
	@Override
	void close() throws SQLException;
}
