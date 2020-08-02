package com.frog.graphql.test.jdbc;

import lombok.Data;

@Data
public class JdbcArg {
	public enum VALUE_TYPE {
        String, StringBetween, StringTable, Number, NumberBetween, NumberTable;
    } 
	private int valueStartIndex;
	private int valueCount;
	private VALUE_TYPE valueType;
	private String argName;
		
	@Override
	public String toString() {
		return String.format("%d-%d-%s-%s", valueStartIndex, valueCount, valueType.name(), argName);
	}
}
