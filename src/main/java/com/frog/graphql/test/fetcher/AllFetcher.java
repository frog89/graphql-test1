package com.frog.graphql.test.fetcher;

import java.util.function.Consumer;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;

import graphql.schema.DataFetchingEnvironment;

@Component
public class AllFetcher {
	
	@Resource
	private JobFetcher jobFetcher;

	@Resource
	private EmployeeFetcher employeeFetcher;

	public void  fetchAll(Consumer<Object> consumer, DataFetchingEnvironment dataFetchingEnvironment) {
		Consumer<Job> jobConsumer = j -> consumer.accept(j);
		jobFetcher.fetchAll(jobConsumer, dataFetchingEnvironment);
		Consumer<Employee> empConsumer = e -> consumer.accept(e);
		employeeFetcher.fetchAll(empConsumer, dataFetchingEnvironment);
	}
}

