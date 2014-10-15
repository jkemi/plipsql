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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;


/**
 * SQL value together with necessary type information
 * @since 1.1
 */
public class SQLParameter {
	public final Object	value;
	public final int	sqlType;
	public final String	typeName;
	public final Integer scaleOrLength;

	public SQLParameter(Object value, int sqlType) {
		this(value,sqlType,null,null);
	}

	public SQLParameter(Object value, int sqlType, String typeName) {
		this(value,sqlType,typeName,null);
	}

	public SQLParameter(Object value, int sqlType, String typeName, Integer scaleOrLength) {
		this.value = value;
		this.sqlType = sqlType;
		this.typeName = typeName;
		this.scaleOrLength = scaleOrLength;
	}

	public SQLParameter(String value) {
		this(value, java.sql.Types.VARCHAR, "VARCHAR");
	}

	public SQLParameter(Short value) {
		this(value, java.sql.Types.SMALLINT, "SMALLINT");
	}

	public SQLParameter(Integer value) {
		this(value, java.sql.Types.INTEGER, "INTEGER");
	}

	public SQLParameter(Long value) {
		this(value, java.sql.Types.BIGINT, "BIGINT");
	}

	public SQLParameter(Float value) {
		this(value, java.sql.Types.REAL, "REAL");
	}

	public SQLParameter(Double value) {
		this(value, java.sql.Types.DOUBLE, "DOUBLE PRECISION");
	}

	public SQLParameter(Boolean value) {
		this(value, java.sql.Types.BOOLEAN, "BOOLEAN");
	}

	public SQLParameter(Date value) {
		this(value, java.sql.Types.TIMESTAMP, "TIMESTAMP");
	}

	public SQLParameter(Timestamp value) {
		this(value, java.sql.Types.TIMESTAMP, "TIMESTAMP");
	}

	/**
	 * @param value
	 * @param scaleOrLength	may be null for defaults
	 */
	public SQLParameter(BigDecimal value, Integer scaleOrLength) {
		this(value, java.sql.Types.DECIMAL, "DECIMAL", scaleOrLength);
	}

	/**
	 *
	 * @param value
	 * @param scaleOrLength	may be null for defaults
	 */
	public SQLParameter(BigInteger value, Integer scaleOrLength) {
		this(value, java.sql.Types.NUMERIC, "NUMERIC", scaleOrLength);
	}

}
