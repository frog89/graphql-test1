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
import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.QueryBuilder;
import com.frog.graphql.test.querybuilder.QueryBuilderArgs;
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

	private QueryBuilderArgs createQueryBuilderArgs(DbTable table, List<DbField> selectFieldList) {
		QueryBuilderArgs sqlBuilderArgs = new QueryBuilderArgs();
		sqlBuilderArgs.getFrom().setFromTable(table);
		sqlBuilderArgs.addSelectFieldList(selectFieldList);
		return sqlBuilderArgs;
	}

	private SqlQuery createAndQuery(DbTable table, List<DbField> selectFieldList, List<QueryConstraint> constraintList) {
		QueryBuilder sqlBuilder = new QueryBuilder();
		QueryBuilderArgs sqlBuilderArgs = createQueryBuilderArgs(table, selectFieldList);
		sqlBuilderArgs.addConstraintList(constraintList);
		return sqlBuilder.createQuery(sqlBuilderArgs);
	}

	private SqlQuery createAndQuery(EmpQueryBuilderArgs args, DbTable table, List<DbField> selectFieldList) {
		return createAndQuery(table, selectFieldList, args.getConstraintList());
	}

	private SqlQuery createOrQuery(EmpQueryBuilderArgs args, DbTable table, List<DbField> selectFieldList) {
		SqlQuery andQuery = createAndQuery(args, table, selectFieldList);
		if (args.getConstraintList() == null || args.getConstraintList().size() <= 1) {
			return andQuery;
		}

		StringBuffer sqlSelectAndFrom = new StringBuffer();
		sqlSelectAndFrom.append("select ");
		sqlSelectAndFrom.append(andQuery.getFieldClause());
		sqlSelectAndFrom.append("\nfrom ");
		sqlSelectAndFrom.append(andQuery.getFromClause());
		
		StringBuffer sql = new StringBuffer();
		for (QueryConstraint constraint : args.getConstraintList()) {			
			if (sql.length() > 0) {
				sql.append("\nunion\n");
			}
			
			sql.append(sqlSelectAndFrom);
			sql.append("\nwhere ");
			sql.append(constraint.getSqlClause());
		}
		if (andQuery.getPageClause().length() > 0) {
			sql.append("\n");
			sql.append(andQuery.getPageClause());			
		}
		
		andQuery.setSql(sql.toString());
		andQuery.setWhereClause(null);
		return andQuery;
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
		EmpDbTable table = empRepository.getTables().get(args.getTableEnum());
		List<DbField> selectFieldList = getSelectFields(table, args.getSelectedGraphQlFields());
		addAdditionalFields(selectFieldList, args.getAdditionalSelectedFieldList());
		
		SqlQuery sqlQuery = null;
		if (args.getSqlOperator() == SqlOperatorEnum.AND) {
			sqlQuery = createAndQuery(args, table, selectFieldList);
		} else { // OR
			sqlQuery = createOrQuery(args, table, selectFieldList);
		}

		return sqlQuery;
	}
}
