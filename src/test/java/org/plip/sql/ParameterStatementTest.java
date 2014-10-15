package org.plip.sql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class ParameterStatementTest {

	@Test
	public void statement1() {
		HashMap<String,List<Integer>> params = new HashMap<>();
		String s= ParameterStatement.parse("SELECT * FROM foo WHERE bar=:bar AND foo=:baz;", params);
		Assert.assertEquals("SELECT * FROM foo WHERE bar=? AND foo=?;", s);

		HashMap<String,List<Integer>> expected = new HashMap<>();
			expected.put("bar", Arrays.asList(1));
			expected.put("baz", Arrays.asList(2));

		Assert.assertEquals(expected, params);
	}

	@Test
	public void statement2() {
		HashMap<String,List<Integer>> params = new HashMap<>();
		String s = ParameterStatement.parse("SELECT * FROM foo WHERE bar=:bar AND foo=:bar;", params);
		Assert.assertEquals("SELECT * FROM foo WHERE bar=? AND foo=?;", s);

		HashMap<String,List<Integer>> expected = new HashMap<>();
			expected.put("bar", Arrays.asList(1,2));

		Assert.assertEquals(expected, params);
	}

	@Test
	public void statement3() {
		HashMap<String,List<Integer>> params = new HashMap<>();
		String s = ParameterStatement.parse("SELECT * FROM foo WHERE bar=:bar,:bar,:baz;", params);

		Assert.assertEquals("SELECT * FROM foo WHERE bar=?,?,?;", s);

		HashMap<String,List<Integer>> expected = new HashMap<>();
			expected.put("bar", Arrays.asList(1,2));
			expected.put("baz", Arrays.asList(3));

		Assert.assertEquals(expected, params);
	}
}
