package com.frog.graphql.test.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
import com.frog.graphql.test.querybuilder.constraint.StringConstraint;
import com.frog.graphql.test.querybuilder.constraint.StringOperatorEnum;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;

@Service
public class JobService {
	@Autowired
	private JdbcService jdbcService;

	@Autowired
	private EmpQueryBuilder queryBuilder;
	
	@Autowired
	private EmpRepository empRepository;
	
	private void find(Consumer<Job> consumer, SqlQuery query) {
		List<DbField> selectFields = query.getSelectFieldList();
		try {
			Consumer<ResultSet> rsConsumer = rs -> {
				Job job = new Job();
				try {
					for (int i=1; i <= selectFields.size(); i++) {
						DbField field = selectFields.get(i-1);
						EmpFieldEnum fieldEnum = EmpFieldEnum.fromOrdinalString(field.getId());
						if (fieldEnum == EmpFieldEnum.JOBS_JOB_ID) {
							job.setId(rs.getString(i));								
						} else if (fieldEnum == EmpFieldEnum.JOBS_JOB_TITLE) {
							job.setJobTitle(rs.getString(i));
						} else if (fieldEnum == EmpFieldEnum.JOBS_MIN_SALARY) {
							job.setMinSalary(rs.getLong(i));
						} else if (fieldEnum == EmpFieldEnum.JOBS_MAX_SALARY) {
							job.setMaxSalary(rs.getLong(i));
						}
					}
					consumer.accept(job);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			
			jdbcService.consumeData(rsConsumer, query.getSql(), query.getArgInfo());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void findAll(Consumer<Job> consumer, DataFetchingEnvironment dataFetchingEnvironment) {
		EmpQueryBuilderArgs args = new EmpQueryBuilderArgs(EmpTableEnum.JOBS);
		List<SelectedField> selectedGraphQlFields = dataFetchingEnvironment.getSelectionSet().getFields("*");
		args.setSelectedGraphQlFields(selectedGraphQlFields);
		SqlQuery query = queryBuilder.createQueryforTable(args);
		find(consumer, query);		
	}

	public void findByEmployees(Consumer<Job> consumer, DataFetchingEnvironment dataFetchingEnvironment, List<Employee> employeeList, 
			List<SelectedField> SelectedGraphQlFields) {
		EmpQueryBuilderArgs args = new EmpQueryBuilderArgs(EmpTableEnum.JOBS);
		args.setSelectedGraphQlFields(SelectedGraphQlFields);
		
		DbField jobIdField = empRepository.getFields().get(EmpFieldEnum.JOBS_JOB_ID);
		List<String> jobIdList = new ArrayList<String>();
		for (int i=0; i<employeeList.size(); i++) {
			Employee emp = employeeList.get(i);
			jobIdList.add(emp.getJobId());
		}		
		args.addConstraint(new StringConstraint(1, jobIdField, StringOperatorEnum.IN, jobIdList));
		
		SqlQuery query = queryBuilder.createQueryforTable(args);
		find(consumer, query);
	}
}
