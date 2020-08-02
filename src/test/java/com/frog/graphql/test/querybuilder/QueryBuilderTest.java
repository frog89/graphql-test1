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
			keyFields.add(new DbField(keyFieldId, keyFieldName, keyFieldName, FieldTypeEnum.STRING));
		}
		List<DbField> fields = new ArrayList<DbField>();
		for (int i=0; i<3; i++) {
			String fieldName = String.format("%s_field%d", alias, i);
			String fieldId = String.format("%s_%s", tableIdString, fieldName); 
			fields.add(new DbField(fieldId, fieldName, fieldName, FieldTypeEnum.STRING));
		}
		table.setKeyFields(keyFields);
		table.setFields(fields);
		return table;
	}
	
	@Test
	void testCreateQuery() {
		QueryBuilder builder = new QueryBuilder();
		DbTable t1 = createTable(0, "mytable1", "t1", 2, 3);
		DbTable t2 = createTable(0, "mytable2", "t2", 2, 3);
		
		SqlFrom from = new SqlFrom();
		from.setFromTable(t1);
		List<SqlJoin> joinList = new ArrayList<SqlJoin>();
		joinList.add(new SqlJoin(JoinKindEnum.INNER, t2, t1));
		from.setJoinList(joinList);
		
		List<DbField> selectFieldList = new ArrayList<DbField>();
		selectFieldList.add(t1.getFields().get(0));
		selectFieldList.add(t1.getFields().get(1));
		selectFieldList.add(t2.getFields().get(1));
		
		List<QueryConstraint> constraintList = new ArrayList<QueryConstraint>();
		constraintList.add(new StringConstraint(1, t2.getFields().get(0), StringOperatorEnum.BEGINS_WITH, "abc"));
		
		SqlQuery query = builder.createQuery(selectFieldList, from, constraintList, SqlOperatorEnum.AND);
		System.out.println(query.getSql());
	}
}
