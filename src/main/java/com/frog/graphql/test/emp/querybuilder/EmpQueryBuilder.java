package com.frog.graphql.test.emp.querybuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.emp.repository.EmpTableEnum;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.QueryBuilder;
import com.frog.graphql.test.querybuilder.SqlFrom;
import com.frog.graphql.test.querybuilder.SqlQuery;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;

import graphql.schema.SelectedField;

@Component
public class EmpQueryBuilder {
	
	@Autowired
	private EmpRepository empRepository;

	private void addAdditionalFields(List<DbField> selectFields, List<DbField> additionalFields) {
		if (additionalFields == null) {
			return;
		}
		for(DbField additionalField : additionalFields) {
			boolean found = false;
			for (DbField field : selectFields) {
				if (field.equals(additionalField)) {
					found = true;
					break;
				}
			}
			if (!found) {
				selectFields.add(additionalField);
			}
		}			
	}
	
	public SqlQuery createQueryforTable(EmpTableEnum tableEnum, 
			List<SelectedField> selectedGraphQlFields,
			List<DbField> additionalSelectedFields,
			List<QueryConstraint> constraintList, SqlOperatorEnum sqlOperator) {
		DbTable table = empRepository.getTables().get(tableEnum);
		List<DbField> selectFieldList = table.findFields(selectedGraphQlFields);
		addAdditionalFields(selectFieldList, additionalSelectedFields);
		
		QueryBuilder builder = new QueryBuilder();
		SqlFrom from = new SqlFrom();
		from.setFromTable(table);
		return builder.createQuery(selectFieldList, from, constraintList, sqlOperator);
	}
}
