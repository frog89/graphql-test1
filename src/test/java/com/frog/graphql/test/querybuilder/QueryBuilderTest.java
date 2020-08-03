package com.frog.graphql.test.querybuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;
import com.frog.graphql.test.querybuilder.constraint.StringConstraint;
import com.frog.graphql.test.querybuilder.constraint.StringOperatorEnum;

@SpringBootTest
class QueryBuilderTest {

	private DbTable createTable(int tableId, String name, String alias, int keyFieldCount, int fieldCount) {
		String tableIdString = "t" + tableId;
		DbTable table = new DbTable(tableIdString, name, alias);
		List<DbField> keyFields = new ArrayList<DbField>();
		for (int i=0; i<keyFieldCount; i++) {
			String keyFieldName = String.format("%s_key%d", alias, i);
			String keyFieldId = String.format("%s_%s", tableIdString, keyFieldName);
			DbField keyField = new DbField(keyFieldId, keyFieldName, keyFieldName, FieldTypeEnum.STRING);
			keyField.setTable(table);
			keyFields.add(keyField);
		}
		List<DbField> fields = new ArrayList<DbField>();
		for (int i=0; i<3; i++) {
			String fieldName = String.format("%s_field%d", alias, i);
			String fieldId = String.format("%s_%s", tableIdString, fieldName); 
			DbField field = new DbField(fieldId, fieldName, fieldName, FieldTypeEnum.STRING);
			field.setTable(table);
			fields.add(field);
		}
		table.setKeyFields(keyFields);
		table.setFields(fields);
		return table;
	}
	
	@Test
	void testCreateQuery() {
		QueryBuilderArgs args = new QueryBuilderArgs();
		QueryBuilder builder = new QueryBuilder();
		DbTable t1 = createTable(0, "mytable1", "t1", 2, 3);
		DbTable t2 = createTable(0, "mytable2", "t2", 2, 3);
		
		args.getFrom().setFromTable(t1);
		List<SqlJoin> joinList = new ArrayList<SqlJoin>();
		joinList.add(new SqlJoin(JoinKindEnum.INNER, t2, t1));
		args.getFrom().setJoinList(joinList);
		
		List<DbField> selectFieldList = new ArrayList<DbField>();
		selectFieldList.add(t1.getFields().get(0));
		selectFieldList.add(t1.getFields().get(1));
		selectFieldList.add(t2.getFields().get(1));
		args.addSelectFieldList(selectFieldList);
		
		args.addConstraint(new StringConstraint(1, t2.getFields().get(0), StringOperatorEnum.BEGINS_WITH, "abc"));
		
		SqlQuery query = builder.createQuery(args);
		System.out.println(query.getSql());
	}
}
