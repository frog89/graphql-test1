//package com.frog.graphql.test.resolver;
//
//import java.util.List;
//
//import com.coxautodev.graphql.tools.GraphQLQueryResolver;
//import com.frog.graphql.test.pojo.Employee;
//import com.frog.graphql.test.pojo.Job;
//import com.frog.graphql.test.service.EmployeeService;
//import com.frog.graphql.test.service.JobService;
//
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//public class Query implements GraphQLQueryResolver {
//
//	private final EmployeeService employeeService;
//	
//	private final JobService jobService;
//		
//	public List<Employee> allEmps() {
//		return employeeService.findAll();
//	}
//	
//	public List<Job> allJobs() {
//		return jobService.findAll();
//	}
//}
