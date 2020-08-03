package com.frog.graphql.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frog.graphql.test.emp.querybuilder.EmpQueryBuilder;
import com.frog.graphql.test.emp.querybuilder.EmpQueryBuilderArgs;
import com.frog.graphql.test.emp.repository.EmpFieldEnum;
import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.emp.repository.EmpTableEnum;
import com.frog.graphql.test.jdbc.JdbcService;
import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.SqlQuery;
import com.frog.graphql.test.querybuilder.constraint.NumericConstraint;
import com.frog.graphql.test.querybuilder.constraint.NumericOperatorEnum;
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
							} else if (fieldEnum == EmpFieldEnum.EMPLOYEES_SALARY) {
								emp.setSalary(rs.getDouble(i));						
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
		Integer testIdCount = dataFetchingEnvironment.getArgument("testIdCount");
		Double salaryBetweenLower = dataFetchingEnvironment.getArgument("salaryBetweenLower");
		Double salaryBetweenHigher = dataFetchingEnvironment.getArgument("salaryBetweenHigher");
		
		EmpQueryBuilderArgs args = new EmpQueryBuilderArgs(EmpTableEnum.EMPLOYEES);
		args.addAdditionalSelectedField(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID));
		args.setSelectedGraphQlFields(dataFetchingEnvironment.getSelectionSet().getFields("*"));

		int constraintIndex = 1;
		if (testIdCount != null) {
			DbField empIdField = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID);
			List<Double> idList = new ArrayList<Double>();
			for (int i=0; i<testIdCount; i++) {
				idList.add(new Double(i));
			}
			args.addConstraint(new NumericConstraint(constraintIndex++, empIdField, NumericOperatorEnum.IN, idList));
		}
		
		if (salaryBetweenLower != null && salaryBetweenHigher != null) {
			DbField salaryField = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_SALARY);
			args.addConstraint(new NumericConstraint(constraintIndex++, salaryField, NumericOperatorEnum.BETWEEN, 
				Arrays.asList(salaryBetweenLower, salaryBetweenHigher)));
		}

		SqlQuery query = queryBuilder.createQueryforTable(args);
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
		EmpQueryBuilderArgs args = new EmpQueryBuilderArgs(EmpTableEnum.EMPLOYEES);
		args.addAdditionalSelectedField(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID));
		
		DbField jobIdField = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID);
		List<String> jobIdList = new ArrayList<String>();
		for (int i=0; i<jobList.size(); i++) {
			Job job = jobList.get(i);
			jobIdList.add(job.getId());
		}
		args.addConstraint(new StringConstraint(1, jobIdField, StringOperatorEnum.IN, jobIdList));
		
		SqlQuery query = queryBuilder.createQueryforTable(args);
		return find(query);
	}
}
