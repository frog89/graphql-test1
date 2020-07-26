package com.frog.graphql.test.fetcher;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.frog.graphql.test.service.JobService;

import graphql.schema.DataFetcher;

@Component
public class JobFetcher {
	
	@Resource
	private JobService jobService;

	public DataFetcher<List<Job>> fetchAll() {
		return dataFetchingEnvironment -> jobService.findAll(dataFetchingEnvironment);
	}

	public DataFetcher<List<Employee>> fetchEmployeesForJob() {
		return dataFetchingEnvironment -> {
			Job job = (Job)dataFetchingEnvironment.getSource();
			return job.getEmployeeList();
		};
	}	
}
