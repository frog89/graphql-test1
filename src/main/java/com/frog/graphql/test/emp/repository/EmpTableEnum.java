package com.frog.graphql.test.emp.repository;

public enum EmpTableEnum {
	EMPLOYEES,
	JOBS;
	
	public static String toOrdinalString(EmpTableEnum value) {
		return String.valueOf(value.ordinal());
	}
	
	public static EmpTableEnum fromOrdinalString(String ordinalString) {
		int ordinal = Integer.parseInt(ordinalString);
		return EmpTableEnum.values()[ordinal];
	}
}
