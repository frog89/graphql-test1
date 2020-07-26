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
public class EmployeeService {
	private final String ENTITY_SQL = "select employee_id, first_name, last_name, job_id from hr.employees"; 
	
	@Autowired
	private JdbcService jdbcService;
		   
	@Autowired
	private JobService jobService;
	
	public List<Employee> find(String sql, JdbcArgInfo argInfo) {
		final ArrayList<Employee> list = new ArrayList<Employee>();
//		String sql = "select employee_id, first_name, last_name from hr.employees " +
//			"where lower(last_name) like '%' || lower(:2) || '%' and " +
//			"employee_id in (select column_value from table(:1))";
		try {
			Consumer<ResultSet> consumer = new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet rs) {
					Employee emp = new Employee();
					try {
						emp.setId(rs.getLong(1));
						emp.setFirstName(rs.getString(2));
						emp.setLastName(rs.getString(3));
						emp.setJobId(rs.getString(4));						
						list.add(emp);
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

	public List<Employee> findAll(DataFetchingEnvironment dataFetchingEnvironment) {
//		List<Double> idList = new ArrayList<Double>();
//		for (int i=0; i<1000000; i++) {
//			idList.add(new Long(i).doubleValue());
//		}
//		JdbcArgInfo argInfo = new JdbcArgInfo();
//		argInfo.addDoubleTableArg("empIds", idList);
//		argInfo.addStringArg("lastNamePart", "as");
//
//		String sql = ENTITY_SQL + " where last_name like '%' || :p2 || '%' and employee_id in (select column_value from table(:p1))";
//		List<Employee> employeeList =  find(sql, argInfo);

		List<Employee> employeeList =  find(ENTITY_SQL, null);
		
		List<Job> jobList = jobService.findByEmployees(employeeList);
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
		return employeeList;
	}

	public List<Employee> findByJobs(List<Job> jobList) {
		List<String> idList = new ArrayList<String>();
		for (int i=0; i<jobList.size(); i++) {
			Job job = jobList.get(i);
			idList.add(job.getId());
		}
		JdbcArgInfo argInfo = new JdbcArgInfo();
		argInfo.addStringTableArg("jobIds", idList);
		
		String whereClause = ENTITY_SQL + " where job_id in (select column_value as job_id from table(:p1))"; 
		return find(whereClause, argInfo);
	}
}
