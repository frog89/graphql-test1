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
			Job job = emp.getJob();
//			if (job == null) {
//				List<SelectedField> selectedGraphQlFields = dataFetchingEnvironment.getSelectionSet().getFields("*");
//				List<Job> jobs = jobService.findByEmployees(dataFetchingEnvironment, Arrays.asList(emp), selectedGraphQlFields);
//				if (jobs.size() == 1) {
//					job = jobs.get(0);
//					emp.setJob(job);
//				}
//			}
			return job;
		};
	}
}

