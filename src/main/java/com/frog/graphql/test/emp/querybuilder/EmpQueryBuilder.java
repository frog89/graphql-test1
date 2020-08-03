package com.frog.graphql.test.emp.querybuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.QueryBuilder;
import com.frog.graphql.test.querybuilder.QueryBuilderArgs;
import com.frog.graphql.test.querybuilder.SqlQuery;

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
	
	public SqlQuery createQueryforTable(EmpQueryBuilderArgs args) {
		DbTable table = empRepository.getTables().get(args.getTableEnum());
		List<DbField> selectFieldList = table.findFields(args.getSelectedGraphQlFields());
		addAdditionalFields(selectFieldList, args.getAdditionalSelectedFieldList());
		
		QueryBuilder sqlBuilder = new QueryBuilder();
		QueryBuilderArgs sqlBuilderArgs = new QueryBuilderArgs();
		sqlBuilderArgs.getFrom().setFromTable(table);
		sqlBuilderArgs.addSelectFieldList(selectFieldList);
		sqlBuilderArgs.addConstraintList(args.getConstraintList());
		
		return sqlBuilder.createQuery(sqlBuilderArgs);
	}
}
