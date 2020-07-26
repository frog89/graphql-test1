package com.frog.graphql.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.jdbc.JdbcService;
import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;

import graphql.schema.DataFetchingEnvironment;

@Service
public class JobService {
	private final String ENTITY_SQL = "select job_id, job_title, min_salary, max_salary from hr.jobs"; 

	@Autowired
	private JdbcService jdbcService;

	@Autowired
	private EmployeeService employeeService;
	
	public List<Job> find(String sql, JdbcArgInfo argInfo) {
		final ArrayList<Job> list = new ArrayList<Job>();
		try {
			Consumer<ResultSet> consumer = new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet rs) {
					Job job = new Job();
					try {
						job.setId(rs.getString(1));
						job.setJobTitle(rs.getString(2));
						job.setMinSalary(rs.getLong(3));
						job.setMaxSalary(rs.getLong(4));
						list.add(job);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
			jdbcService.consumeData(sql, argInfo, consumer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public List<Job> findAll(DataFetchingEnvironment dataFetchingEnvironment) {
		List<Job> jobList = find(ENTITY_SQL, null); 
		List<Employee> employees = employeeService.findByJobs(jobList);
		for (int i=0; i<jobList.size(); i++) {
			Job job = jobList.get(i);
			List<Employee> filteredList = employees.stream().filter(e -> e.getJobId().equals(job.getId()))
				.collect(Collectors.toList()); 
			job.setEmployeeList(filteredList);
		} 
		return jobList;
	}

	public List<Job> findByEmployees(List<Employee> employeeList) {
		List<String> idList = new ArrayList<String>();
		for (int i=0; i<employeeList.size(); i++) {
			Employee emp = employeeList.get(i);
			idList.add(emp.getJobId());
		}
		JdbcArgInfo argInfo = new JdbcArgInfo();
		argInfo.addStringTableArg("jobIds", idList);
		
		String whereClause = ENTITY_SQL + " where job_id in (select column_value as job_id from table(:p1))"; 
		return find(whereClause, argInfo);
	}
}
