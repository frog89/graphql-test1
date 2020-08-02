package com.frog.graphql.test.emp.repository;

public enum EmpFieldEnum {
	EMPLOYEES_EMPLOYEE_ID,
	EMPLOYEES_FIRST_NAME,
	EMPLOYEES_LAST_NAME,
	EMPLOYEES_JOB_ID,
	JOBS_JOB_ID,
	JOBS_JOB_TITLE,
	JOBS_MIN_SALARY,
	JOBS_MAX_SALARY;
	
	public static String toOrdinalString(EmpFieldEnum value) {
		return String.valueOf(value.ordinal());
	}
	
	public static EmpFieldEnum fromOrdinalString(String ordinalString) {
		int ordinal = Integer.parseInt(ordinalString);
		return EmpFieldEnum.values()[ordinal];
	}
}
