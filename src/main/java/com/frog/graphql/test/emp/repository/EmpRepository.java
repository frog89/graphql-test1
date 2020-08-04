package com.frog.graphql.test.emp.repository;

import java.util.Hashtable;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.FieldTypeEnum;

import lombok.Data;

@Component
@Data
public class EmpRepository {
	private Hashtable<EmpTableEnum, EmpDbTable> tables;
	private Hashtable<EmpFieldEnum, EmpDbField> fields;

	private void addTable(EmpDbTable table) {
		EmpTableEnum tableEnum = EmpTableEnum.fromOrdinalString(table.getId());
		tables.put(tableEnum, table);
	}

	private void addField(EmpDbField field) {
		EmpFieldEnum fieldEnum = EmpFieldEnum.fromOrdinalString(field.getId());
		fields.put(fieldEnum, field);
	}
	
	private void addTableKeyField(EmpTableEnum tableEnum, EmpFieldEnum fieldEnum) {
		tables.get(tableEnum).addKeyField(fields.get(fieldEnum));
	}

	private void addTableFields() {
		for (EmpFieldEnum fieldEnum : EmpFieldEnum.values()) {
			DbField field = fields.get(fieldEnum);
			field.getTable().addField(field);
		}
	}
	
	@PostConstruct
	public void init() {
		
		tables = new Hashtable<EmpTableEnum, EmpDbTable>();
		addTable(new EmpDbTable(EmpTableEnum.EMPLOYEES, "hr.employees", "e"));
		addTable(new EmpDbTable(EmpTableEnum.JOBS, "hr.jobs", "j"));
		
		fields = new Hashtable<EmpFieldEnum, EmpDbField>();
		addField(new EmpDbField(EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID, 
			"id","EMPLOYEE_ID", FieldTypeEnum.LONG, null, tables.get(EmpTableEnum.EMPLOYEES)));
		addField(new EmpDbField(EmpFieldEnum.EMPLOYEES_FIRST_NAME, 
			"firstName", "FIRST_NAME", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES)));
		addField(new EmpDbField(EmpFieldEnum.EMPLOYEES_LAST_NAME, 
			"lastName", "LAST_NAME", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES)));
		addField(new EmpDbField(EmpFieldEnum.EMPLOYEES_JOB_ID, 
			"jobId", "JOB_ID", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.EMPLOYEES)));
		addField(new EmpDbField(EmpFieldEnum.EMPLOYEES_SALARY, 
			"salary", "SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.EMPLOYEES)));

		addField(new EmpDbField(EmpFieldEnum.JOBS_JOB_ID, 
			"id", "JOB_ID", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.JOBS)));
		addField(new EmpDbField(EmpFieldEnum.JOBS_JOB_TITLE, 
			"jobTitle", "JOB_TITLE", FieldTypeEnum.STRING, null, tables.get(EmpTableEnum.JOBS)));
		addField(new EmpDbField(EmpFieldEnum.JOBS_MIN_SALARY, 
			"minSalary", "MIN_SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.JOBS)));
		addField(new EmpDbField(EmpFieldEnum.JOBS_MAX_SALARY, 
			"maxSalary", "MAX_SALARY", FieldTypeEnum.DOUBLE, null, tables.get(EmpTableEnum.JOBS)));
		
		addTableFields();
		addTableKeyField(EmpTableEnum.EMPLOYEES, EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID);
		addTableKeyField(EmpTableEnum.JOBS, EmpFieldEnum.JOBS_JOB_ID);
	}
}
