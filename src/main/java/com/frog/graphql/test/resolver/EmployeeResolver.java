//package com.frog.graphql.test.resolver;
//
//import com.coxautodev.graphql.tools.GraphQLResolver;
//import com.frog.graphql.test.pojo.Employee;
//import com.frog.graphql.test.pojo.Job;
//import com.frog.graphql.test.service.JobService;
//
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//public class EmployeeResolver implements GraphQLResolver<Employee> {
//	
//	private final JobService jobService;
//	
//	public Job job(Employee emp) {
//		return jobService.findJobForEmployee(emp);
//	}
//}
