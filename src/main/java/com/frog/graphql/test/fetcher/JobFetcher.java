package com.frog.graphql.test.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.frog.graphql.test.service.EmployeeService;
import com.frog.graphql.test.service.JobService;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;

@Component
public class JobFetcher extends EmpDataFetcher {
	
	@Resource
	private JobService jobService;

	@Resource
	private EmployeeService employeeService;
	
	private void fetchDetails(DataFetchingEnvironment dataFetchingEnvironment, List<Job> jobList) {
		List<SelectedField> selectedEmpFields = dataFetchingEnvironment.getSelectionSet().getFields("employees/*");
		if (selectedEmpFields.size() > 0) {
			List<Employee> allJobsEmpList = new ArrayList<Employee>();
			Consumer<Employee> allJobsEmpConsumer = e -> allJobsEmpList.add(e);
			employeeService.findByJobs(allJobsEmpConsumer, jobList, selectedEmpFields);
			for (int i=0; i<jobList.size(); i++) {
				Job job = jobList.get(i);
				List<Employee> jobEmpList = allJobsEmpList.stream().
					filter(e -> e.getJobId().equals(job.getId())).
					collect(Collectors.toList()); 
				job.setEmployeeList(jobEmpList);
			}			
		}
	}
	
	public void fetchAll(Consumer<Job> consumer, DataFetchingEnvironment dataFetchingEnvironment) {
		List<Job> bufferedList = new ArrayList<Job>();
		Consumer<Job> bufferedConsumer = bufferedItem -> {
			bufferedList.add(bufferedItem);				
			if (bufferedList.size() >= BUFFER_SIZE) {
				fetchDetails(dataFetchingEnvironment, bufferedList);
				bufferedList.forEach(item -> consumer.accept(item));
				bufferedList.clear();
			}
		};
		jobService.findAll(bufferedConsumer, dataFetchingEnvironment);		
		if (bufferedList.size() > 0) {
			fetchDetails(dataFetchingEnvironment, bufferedList);
			bufferedList.forEach(item -> consumer.accept(item));
		}
	}

	public DataFetcher<List<Employee>> fetchEmployeesForJob() {
		return dataFetchingEnvironment -> {
			Job job = (Job)dataFetchingEnvironment.getSource();
			return job.getEmployeeList();
		};
	}	
}
