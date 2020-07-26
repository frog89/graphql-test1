package com.frog.graphql.test.jdbc;

import lombok.Data;

@Data
public class JdbcArg {
	public enum VALUE_TYPE {
        String, StringTable, Number, NumberTable;
    } 
	private int valueStartIndex;
	private int valueCount;
	private VALUE_TYPE valueType;
	private String argName;
	
	private String getValueTypeString() {
		if (valueType == VALUE_TYPE.String) {
			return "string";
		}
		if (valueType == VALUE_TYPE.Number) {
			return "number";
		}
		if (valueType == VALUE_TYPE.StringTable) {
			return "string_table";
		}
		if (valueType == VALUE_TYPE.NumberTable) {
			return "number_table";
		}
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("%d-%d-%s-%s", valueStartIndex, valueCount, getValueTypeString(), argName);
	}
}
