package com.frog.graphql.test.emp.repository;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.FieldTypeEnum;

import lombok.Getter;

@Component
public class EmpRepository {
	@Getter private Hashtable<EmpTableEnum, EmpDbTable> tables;
	@Getter private Hashtable<EmpFieldEnum, EmpDbField> fields;
	private Hashtable<String, List<EmpDbField>> graphQlAliasMappings;

	private void addTable(EmpDbTable table) {
		EmpTableEnum tableEnum = EmpTableEnum.fromOrdinalString(table.getId());
		tables.put(tableEnum, table);
	}

	private void addField(String graphQlAlias, EmpDbField field) {
		addGraphQlMapping(field.getTable(), graphQlAlias, field);
		EmpFieldEnum fieldEnum = EmpFieldEnum.fromOrdinalString(field.getId());
		fields.put(fieldEnum, field);
	}
	
	private void addTableKeyField(EmpTableEnum tableEnum, EmpFieldEnum fieldEnum) {
		tables.get(tableEnum).addKeyField(fields.get(fieldEnum));
	}

	private void addTableFields() {
		for (EmpFieldEnum fieldEnum : EmpFieldEnum.values()) {
			DbField field = fields.get(fieldEnum);
			if (field == null) {
				continue;
			}
			field.getTable().addField(field);
		}
	}
	
	private String getGraphQlMappingKey(DbTable table, String graphQlAlias) {
		return String.format("%s-%s", table.getDbAlias(), graphQlAlias);
	}
	
	private void addGraphQlMapping(DbTable table, String graphQlAlias, EmpDbField... fields) {
		String key = getGraphQlMappingKey(table, graphQlAlias);
		graphQlAliasMappings.put(key, Arrays.asList(fields));
	}
	
	public boolean containsGraphQlMapping(EmpDbTable table, String graphQlAlias) {
		String key = getGraphQlMappingKey(table, graphQlAlias);
		return graphQlAliasMappings.containsKey(key);
	}

	public List<EmpDbField> getGraphQlMapping(EmpDbTable table, String graphQlAlias) {
		String key = getGraphQlMappingKey(table, graphQlAlias);
		return graphQlAliasMappings.get(key);
	}
	
	@PostConstruct
	public void init() {
		graphQlAliasMappings = new Hashtable<String, List<EmpDbField>>();
		
		tables = new Hashtable<EmpTableEnum, EmpDbTable>();
		addTable(new EmpDbTable(EmpTableEnum.EMPLOYEES, "hr.employees", "e"));
		addTable(new EmpDbTable(EmpTableEnum.JOBS, "hr.jobs", "j"));
		
		fields = new Hashtable<EmpFieldEnum, EmpDbField>();
		addField("id", new EmpDbField(EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID, 
			"EMPLOYEE_ID", FieldTypeEnum.LONG, null, tables.get(EmpTableEnum.EMPLOYEES)));
		EmpDbField firstNameField = new EmpDbField(EmpFieldEnum.EMPLOYEES_FIRST_NAME, 
			"FIRST_NAME", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES));
		EmpDbField lastNameField = new EmpDbField(EmpFieldEnum.EMPLOYEES_LAST_NAME,
			"LAST_NAME", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES));
		addField("firstName", firstNameField);
		addField("lastName", lastNameField);
		addGraphQlMapping(tables.get(EmpTableEnum.EMPLOYEES), "fullName", firstNameField, lastNameField);
		addField("jobId", new EmpDbField(EmpFieldEnum.EMPLOYEES_JOB_ID, 
			"JOB_ID", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES)));
		addField("salary", new EmpDbField(EmpFieldEnum.EMPLOYEES_SALARY, 
			"SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.EMPLOYEES)));

		addField("id",new EmpDbField(EmpFieldEnum.JOBS_JOB_ID, 
			"JOB_ID", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.JOBS)));
		addField("jobTitle", new EmpDbField(EmpFieldEnum.JOBS_JOB_TITLE, 
			"JOB_TITLE", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.JOBS)));
		addField("minSalary", new EmpDbField(EmpFieldEnum.JOBS_MIN_SALARY, 
			"MIN_SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.JOBS)));
		addField("maxSalary", new EmpDbField(EmpFieldEnum.JOBS_MAX_SALARY, 
			"MAX_SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.JOBS)));
		
		addTableFields();
		addTableKeyField(EmpTableEnum.EMPLOYEES, EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID);
		addTableKeyField(EmpTableEnum.JOBS, EmpFieldEnum.JOBS_JOB_ID);
	}
}
