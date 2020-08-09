package com.frog.graphql.test.emp.querybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frog.graphql.test.emp.repository.EmpDbField;
import com.frog.graphql.test.emp.repository.EmpDbTable;
import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.QueryBuilder;
import com.frog.graphql.test.querybuilder.QueryBuilderArgs;
import com.frog.graphql.test.querybuilder.SqlQuery;
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
	
	private QueryBuilderArgs createSqlBuilderArgs(EmpQueryBuilderArgs args) {
		EmpDbTable table = empRepository.getTables().get(args.getTableEnum());
		List<DbField> selectFieldList = getSelectFields(table, args.getSelectedGraphQlFields());
		addAdditionalFields(selectFieldList, args.getAdditionalSelectedFieldList());

		QueryBuilderArgs sqlBuilderArgs = new QueryBuilderArgs();
		sqlBuilderArgs.getFrom().setFromTable(table);
		sqlBuilderArgs.addSelectFieldList(selectFieldList);
		sqlBuilderArgs.addConstraintList(args.getConstraintList());
		return sqlBuilderArgs;
	}

	private SqlQuery createAndQuery(EmpQueryBuilderArgs args) {
		QueryBuilderArgs sqlBuilderArgs = createSqlBuilderArgs(args);
		QueryBuilder sqlBuilder = new QueryBuilder();
		return sqlBuilder.createAndQuery(sqlBuilderArgs);
	}

	private SqlQuery createOrQuery(EmpQueryBuilderArgs args) {
		QueryBuilderArgs sqlBuilderArgs = createSqlBuilderArgs(args);
		QueryBuilder sqlBuilder = new QueryBuilder();
		return sqlBuilder.createOrQuery(sqlBuilderArgs);
	}
	
	public List<DbField> getSelectFields(EmpDbTable table, List<SelectedField> selectedGraphQlFields) {
		List<DbField> selectFieldList = new ArrayList<DbField>();

		List<SelectedField> graphQlFields = selectedGraphQlFields == null ?
			Collections.emptyList() : selectedGraphQlFields;
		for (SelectedField graphQlField : graphQlFields) {
			String graphQlAlias = graphQlField.getName();
			if (!empRepository.containsGraphQlMapping(table, graphQlAlias)) {
				continue;
			}
			List<EmpDbField> fields = empRepository.getGraphQlMapping(table, graphQlAlias);
			selectFieldList.addAll(fields);
		}

		// Keyfields are always fetched from database
		for (DbField keyField : table.getKeyFields()) {
			if (!selectFieldList.contains(keyField)) {
				selectFieldList.add(keyField);
			}
		}

		return selectFieldList;		
	}

	public SqlQuery createQueryforTable(EmpQueryBuilderArgs args) {		
		SqlQuery sqlQuery = null;
		if (args.getSqlOperator() == SqlOperatorEnum.AND) {
			sqlQuery = createAndQuery(args);
		} else { // OR
			sqlQuery = createOrQuery(args);
		}

		return sqlQuery;
	}
}
