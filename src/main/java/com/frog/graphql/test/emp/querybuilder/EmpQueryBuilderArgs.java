package com.frog.graphql.test.emp.querybuilder;

import java.util.ArrayList;
import java.util.List;

import com.frog.graphql.test.emp.repository.EmpTableEnum;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.QueryExecutionParameters;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;

import graphql.schema.SelectedField;
import lombok.Getter;
import lombok.Setter;

public class EmpQueryBuilderArgs {
	@Getter @Setter private EmpTableEnum tableEnum; 
	@Getter @Setter private List<SelectedField> selectedGraphQlFields;
	@Getter private List<DbField> additionalSelectedFieldList;
	@Getter private List<QueryConstraint> constraintList; 
	@Getter @Setter private SqlOperatorEnum sqlOperator = SqlOperatorEnum.AND;
	@Getter QueryExecutionParameters executionParameters;
	
	public EmpQueryBuilderArgs() {
		this.additionalSelectedFieldList = new ArrayList<DbField>();
		this.constraintList = new ArrayList<QueryConstraint>();
		this.executionParameters = new QueryExecutionParameters();
	}
	
	public EmpQueryBuilderArgs(EmpTableEnum tableEnum) {
		this();
		this.tableEnum = tableEnum;
	}
	
	public void addAdditionalSelectedField(DbField field) {
		this.additionalSelectedFieldList.add(field);
	}
	public void addAdditionalSelectedFieldList(List<DbField> fieldList) {
		this.additionalSelectedFieldList.addAll(fieldList);
	}

	public void addConstraint(QueryConstraint constraint) {
		this.constraintList.add(constraint);
	}
	public void addConstraintList(List<QueryConstraint> constraintList) {
		this.constraintList.addAll(constraintList);
	}
}
