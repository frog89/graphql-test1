package com.frog.graphql.test.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Job {
	private String id;
	private String jobTitle;
	private long minSalary;
	private long maxSalary;
	
	private List<Employee> employeeList;
	
	public void setEmployeeList(List<Employee> aEmployeeList) {
		employeeList = new ArrayList<Employee>();
		employeeList.addAll(aEmployeeList);
	}
	public List<Employee> getEmployeeList() {
		return employeeList;
	}
}
