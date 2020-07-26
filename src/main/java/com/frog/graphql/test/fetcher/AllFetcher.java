package com.frog.graphql.test.fetcher;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.service.EmployeeService;
import com.frog.graphql.test.service.JobService;

import graphql.schema.DataFetcher;

@Component
public class AllFetcher {
	
	@Resource
	private JobService jobService;

	@Resource
	private EmployeeService employeeService;

	public DataFetcher<List<Object>> fetchAll() {
		return  dataFetchingEnvironment -> {
			List list1 = jobService.findAll(dataFetchingEnvironment);
			List list2 = employeeService.findAll(dataFetchingEnvironment);
			list1.addAll(list2);
			return list1;
		};
	}
}

