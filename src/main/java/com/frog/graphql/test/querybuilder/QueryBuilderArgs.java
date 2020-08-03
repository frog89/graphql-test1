package com.frog.graphql.test.querybuilder;

import java.util.ArrayList;
import java.util.List;

import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;

import lombok.Getter;
import lombok.Setter;

public class QueryBuilderArgs {
	@Getter private List<DbField> selectFieldList; 
	@Getter private List<QueryConstraint> constraintList;
	@Getter private SqlFrom from; 
	@Getter QueryExecutionParameters executionParameters;
	@Getter @Setter private SqlOperatorEnum sqlOperator = SqlOperatorEnum.AND;
	
	public QueryBuilderArgs() {
		this.selectFieldList = new ArrayList<DbField>();
		this.constraintList = new ArrayList<QueryConstraint>();
		this.from = new SqlFrom();
		this.executionParameters = new QueryExecutionParameters();
	}
	
	public QueryBuilderArgs(SqlOperatorEnum sqlOperator) {
		this();
		this.sqlOperator = sqlOperator;
	}

	public void addSelectField(DbField field) {
		this.selectFieldList.add(field);
	}
	public void addSelectFieldList(List<DbField> fieldList) {
		this.selectFieldList.addAll(fieldList);
	}

	public void addConstraint(QueryConstraint constraint) {
		this.constraintList.add(constraint);
	}
	public void addConstraintList(List<QueryConstraint> constraintList) {
		this.constraintList.addAll(constraintList);
	}
}
