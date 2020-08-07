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
public class EmployeeFetcher extends EmpDataFetcher {
	
	@Resource
	private JobService jobService;

	@Resource
	private EmployeeService employeeService;

	private void fetchDetails(DataFetchingEnvironment dataFetchingEnvironment, List<Employee> employeeList) {
		List<SelectedField> jobFields = dataFetchingEnvironment.getSelectionSet().getFields("job/*");
		if (jobFields.size() > 0) {
			List<Job> jobList = new ArrayList<Job>();
			Consumer<Job> jobConsumer = j -> jobList.add(j); 
					
			jobService.findByEmployees(jobConsumer, dataFetchingEnvironment, employeeList, jobFields);
			for (int i=0; i<employeeList.size(); i++) {
				Employee emp = employeeList.get(i);
				List<Job> filteredList = jobList.stream()
					.filter(j -> j.getId().equals(emp.getJobId()))
					.collect(Collectors.toList());
				if (filteredList.size() == 1) {
					emp.setJob(filteredList.get(0));
				} else {
					emp.setJob(null);
				} 
			}
			
			List<SelectedField> jobEmployeesFields = dataFetchingEnvironment.getSelectionSet().getFields("job/employees/*");
			if (jobEmployeesFields.size() > 0) {
				List<Employee> allJobsEmployeeList = new ArrayList<Employee>();
				Consumer<Employee> allJobsEmpConsumer = e -> allJobsEmployeeList.add(e);   
				employeeService.findByJobs(allJobsEmpConsumer, jobList, jobEmployeesFields);
				for (Job job : jobList) {
					List<Employee> jobEmpList = allJobsEmployeeList.stream().
						filter(e -> e.getJobId().equals(job.getId())).
						collect(Collectors.toList());
					job.setEmployeeList(jobEmpList);
				}
				
			}
		}
	}
	
	public void fetchAll(Consumer<Employee> consumer, DataFetchingEnvironment dataFetchingEnvironment) {
		List<Employee> bufferedList = new ArrayList<Employee>();
		Consumer<Employee> bufferedConsumer = bufferedItem -> {
			bufferedList.add(bufferedItem);				
			if (bufferedList.size() >= BUFFER_SIZE) {
				fetchDetails(dataFetchingEnvironment, bufferedList);
				bufferedList.forEach(item -> consumer.accept(item));
				bufferedList.clear();
			}
		};
		employeeService.findAll(bufferedConsumer, dataFetchingEnvironment);
		if (bufferedList.size() > 0) {
			fetchDetails(dataFetchingEnvironment, bufferedList);
			bufferedList.forEach(item -> consumer.accept(item));
		}
	}

	public DataFetcher<Job> fetchJobForEmployee() {
		return dataFetchingEnvironment -> {
			Employee emp = (Employee)dataFetchingEnvironment.getSource();
			return emp.getJob();
		};
	}
}

