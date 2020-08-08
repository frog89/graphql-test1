package com.frog.graphql.test.emp.repository;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.FieldTypeEnum;

import lombok.Getter;

@Component
public class EmpRepository {
	@Getter private Hashtable<EmpTableEnum, EmpDbTable> tables;
	@Getter private Hashtable<EmpFieldEnum, EmpDbField> fields;
	private Hashtable<String, List<EmpDbField>> graphQlAliasMappings;

	private EmpDbTable addTable(EmpTableEnum empTableEnum, String dbExpression, String dbAlias) {
		EmpDbTable table = new EmpDbTable(empTableEnum, dbExpression, dbAlias);
		EmpTableEnum tableEnum = EmpTableEnum.fromOrdinalString(table.getId());
		tables.put(tableEnum, table);
		return table;
	}

	private EmpDbField addField(EmpDbTable table, String graphQlAlias, EmpFieldEnum empFieldEnum, String fieldName, FieldTypeEnum fieldTypeEnum,
			KeyFieldKindEnum keyFieldKindEnum, String fieldAlias) {
		EmpDbField field = new EmpDbField(empFieldEnum, fieldName, fieldTypeEnum, fieldAlias, table);
		addGraphQlMapping(field.getTable(), graphQlAlias, field);
		EmpFieldEnum fieldEnum = EmpFieldEnum.fromOrdinalString(field.getId());
		fields.put(fieldEnum, field);
		table.addField(field);
		if (keyFieldKindEnum != KeyFieldKindEnum.NO_KEY && !keyFieldKindEnum.name().startsWith("REF_")) {
			table.addKeyField(field);
		}
		return field;
	}

	private EmpDbField addField(EmpDbTable table, String graphQlAlias, EmpFieldEnum empFieldEnum, String fieldName, FieldTypeEnum fieldTypeEnum,
			KeyFieldKindEnum keyFieldKindEnum) {
		return addField(table, graphQlAlias, empFieldEnum, fieldName, fieldTypeEnum, keyFieldKindEnum, null);
	}

	private EmpDbField addField(EmpDbTable table, String graphQlAlias, EmpFieldEnum empFieldEnum, String fieldName, FieldTypeEnum fieldTypeEnum) {
		return addField(table, graphQlAlias, empFieldEnum, fieldName, fieldTypeEnum, KeyFieldKindEnum.NO_KEY, null);
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
		EmpDbTable empTable = addTable(EmpTableEnum.EMPLOYEES, "hr.employees", "e");
		EmpDbTable jobTable = addTable(EmpTableEnum.JOBS, "hr.jobs", "j");
		
		fields = new Hashtable<EmpFieldEnum, EmpDbField>();
		addField(empTable, "id", EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID, "EMPLOYEE_ID", FieldTypeEnum.LONG, KeyFieldKindEnum.EMPLOYEE_ID);
		EmpDbField firstNameField = addField(empTable, "firstName", EmpFieldEnum.EMPLOYEES_FIRST_NAME, "FIRST_NAME", FieldTypeEnum.STRING);
		EmpDbField lastNameField = addField(empTable, "lastName", EmpFieldEnum.EMPLOYEES_LAST_NAME, "LAST_NAME", FieldTypeEnum.STRING);
		addGraphQlMapping(empTable, "fullName", firstNameField, lastNameField);
		addField(empTable, "jobId", EmpFieldEnum.EMPLOYEES_JOB_ID, "JOB_ID", FieldTypeEnum.STRING, KeyFieldKindEnum.REF_JOB_ID);
		addField(empTable, "salary", EmpFieldEnum.EMPLOYEES_SALARY, "SALARY", FieldTypeEnum.DOUBLE);

		addField(jobTable, "id", EmpFieldEnum.JOBS_JOB_ID, "JOB_ID", FieldTypeEnum.STRING, KeyFieldKindEnum.JOB_ID);
		addField(jobTable, "jobTitle", EmpFieldEnum.JOBS_JOB_TITLE, "JOB_TITLE", FieldTypeEnum.STRING);
		addField(jobTable, "minSalary", EmpFieldEnum.JOBS_MIN_SALARY, "MIN_SALARY", FieldTypeEnum.DOUBLE);
		addField(jobTable, "maxSalary", EmpFieldEnum.JOBS_MAX_SALARY, "MAX_SALARY", FieldTypeEnum.DOUBLE);
	}
}
