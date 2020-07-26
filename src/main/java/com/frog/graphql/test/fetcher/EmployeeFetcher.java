package com.frog.graphql.test.fetcher;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.frog.graphql.test.service.EmployeeService;
import com.frog.graphql.test.service.JobService;

import graphql.schema.DataFetcher;

@Component
public class EmployeeFetcher {
	
	@Resource
	private JobService jobService;

	@Resource
	private EmployeeService employeeService;

	public DataFetcher<List<Employee>> fetchAll() {
		return  dataFetchingEnvironment -> employeeService.findAll(dataFetchingEnvironment);
	}

	public DataFetcher<Job> fetchJobForEmployee() {
		return dataFetchingEnvironment -> {
			Employee emp = (Employee)dataFetchingEnvironment.getSource();
			return emp.getJob();
		};
	}
}

