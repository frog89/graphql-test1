package com.frog.graphql.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frog.graphql.test.emp.querybuilder.EmpQueryBuilder;
import com.frog.graphql.test.emp.repository.EmpFieldEnum;
import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.emp.repository.EmpTableEnum;
import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.jdbc.JdbcService;
import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.SqlQuery;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;
import com.frog.graphql.test.querybuilder.constraint.StringConstraint;
import com.frog.graphql.test.querybuilder.constraint.StringOperatorEnum;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;

@Service
public class EmployeeService {	
	@Autowired
	private JdbcService jdbcService;
		   
	@Autowired
	private JobService jobService;
	
	@Autowired
	private EmpQueryBuilder queryBuilder;

	@Autowired
	private EmpRepository empRepository;
	
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
	
	public List<Employee> find(SqlQuery query) {
		List<DbField> selectFields = query.getSelectFieldList();
		final ArrayList<Employee> list = new ArrayList<Employee>();
		try {
			Consumer<ResultSet> consumer = new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet rs) {
					Employee emp = new Employee();
					try {
						for (int i=1; i <= selectFields.size(); i++) {
							DbField field = selectFields.get(i-1);
							EmpFieldEnum fieldEnum = EmpFieldEnum.fromOrdinalString(field.getId());
							if (fieldEnum == EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID) {
								emp.setId(rs.getLong(i));
							} else if (fieldEnum == EmpFieldEnum.EMPLOYEES_FIRST_NAME) {
								emp.setFirstName(rs.getString(i));
							} else if (fieldEnum == EmpFieldEnum.EMPLOYEES_LAST_NAME) {
								emp.setLastName(rs.getString(i));
							} else if (fieldEnum == EmpFieldEnum.EMPLOYEES_JOB_ID) {
								emp.setJobId(rs.getString(i));						
							}
						}
						list.add(emp);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
			jdbcService.consumeData(query.getSql(), query.getArgInfo(), consumer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}


	public List<Employee> findAll(DataFetchingEnvironment dataFetchingEnvironment) {
		List<SelectedField> selectedFields = dataFetchingEnvironment.getSelectionSet().getFields("*");
		List<DbField> additionalFields = new ArrayList<DbField>();
		additionalFields.add(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID));
		SqlQuery query = queryBuilder.createQueryforTable(EmpTableEnum.EMPLOYEES, 
			selectedFields, additionalFields, null, SqlOperatorEnum.AND);
		List<Employee> employeeList = find(query);
		
		List<Job> jobList = jobService.findByEmployees(dataFetchingEnvironment, employeeList);
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

	public List<Employee> findByJobs(List<Job> jobList, List<SelectedField> selectedEmpFields) {
		List<String> jobIdList = new ArrayList<String>();
		for (int i=0; i<jobList.size(); i++) {
			Job job = jobList.get(i);
			jobIdList.add(job.getId());
		}
		DbField field = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID);
		List<QueryConstraint> constraintList = new ArrayList<QueryConstraint>();
		StringConstraint jobIdConstraint = new StringConstraint(1, field, StringOperatorEnum.IN, jobIdList);
		constraintList.add(jobIdConstraint);
		
		List<DbField> additionalFields = new ArrayList<DbField>();
		additionalFields.add(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID));
		SqlQuery query = queryBuilder.createQueryforTable(EmpTableEnum.EMPLOYEES, selectedEmpFields, additionalFields, 
			constraintList, SqlOperatorEnum.AND);
		return find(query);
	}
}
