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
public class JobService {
	@Autowired
	private JdbcService jdbcService;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private EmpQueryBuilder queryBuilder;
	
	@Autowired
	private EmpRepository empRepository;
	
	public List<Job> find(SqlQuery query) {
		List<DbField> selectFields = query.getSelectFieldList();
		final ArrayList<Job> list = new ArrayList<Job>();
		try {
			Consumer<ResultSet> consumer = new Consumer<ResultSet>() {
				@Override
				public void accept(ResultSet rs) {
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
						list.add(job);
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

	public List<Job> findAll(DataFetchingEnvironment dataFetchingEnvironment) {
		List<SelectedField> selectedFields = dataFetchingEnvironment.getSelectionSet().getFields("*");
		SqlQuery query = queryBuilder.createQueryforTable(EmpTableEnum.JOBS, selectedFields, null, null, SqlOperatorEnum.AND);
		List<Job> jobList = find(query);
		
		List<SelectedField> selectedEmpFields = dataFetchingEnvironment.getSelectionSet().getFields("employees/*");
		List<Employee> employees = employeeService.findByJobs(jobList, selectedEmpFields);
		for (int i=0; i<jobList.size(); i++) {
			Job job = jobList.get(i);
			List<Employee> filteredList = employees.stream().filter(e -> e.getJobId().equals(job.getId()))
				.collect(Collectors.toList()); 
			job.setEmployeeList(filteredList);
		} 
		return jobList;
	}

	public List<Job> findByEmployees(DataFetchingEnvironment dataFetchingEnvironment, List<Employee> employeeList) {
		List<String> jobIdList = new ArrayList<String>();
		for (int i=0; i<employeeList.size(); i++) {
			Employee emp = employeeList.get(i);
			jobIdList.add(emp.getJobId());
		}		
		DbField field = empRepository.getFields().get(EmpFieldEnum.JOBS_JOB_ID);
		List<QueryConstraint> constraintList = new ArrayList<QueryConstraint>();
		StringConstraint jobIdConstraint = new StringConstraint(1, field, StringOperatorEnum.IN, jobIdList);
		constraintList.add(jobIdConstraint);
		
		List<SelectedField> selectedFields = dataFetchingEnvironment.getSelectionSet().getFields("job/*");
		SqlQuery query = queryBuilder.createQueryforTable(EmpTableEnum.JOBS, selectedFields, null,
			constraintList, SqlOperatorEnum.AND);
		return find(query);
	}
}
