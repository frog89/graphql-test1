package com.frog.graphql.test.querybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
			DbField keyField = new DbField(keyFieldId, keyFieldName, FieldTypeEnum.STRING);
			keyField.setTable(table);
			keyFields.add(keyField);
		}
		List<DbField> fields = new ArrayList<DbField>();
		for (int i=0; i<3; i++) {
			String fieldName = String.format("%s_field%d", alias, i);
			String fieldId = String.format("%s_%s", tableIdString, fieldName); 
			DbField field = new DbField(fieldId, fieldName, FieldTypeEnum.STRING);
			field.setTable(table);
			fields.add(field);
		}
		table.setKeyFields(keyFields);
		table.setFields(fields);
		return table;
	}
	
	private QueryBuilderArgs createQueryExampleArgs() {
		QueryBuilderArgs args = new QueryBuilderArgs();
		
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
		
		return args;
	}
	
	@Test
	void testCreateAndQuery1() {
		QueryBuilderArgs args = createQueryExampleArgs();
		DbTable fromTable = args.getFrom().getFromTable();
		DbField f0 = fromTable.getFields().get(0);
		args.addConstraint(new StringConstraint(1, f0, StringOperatorEnum.EQUALS, "abc"));
		
		QueryBuilder builder = new QueryBuilder();
		SqlQuery query = builder.createAndQuery(args);
		System.out.println(query.getSql());
		
		String expectedWhere = String.format("%s = :p1", f0.getFullName()); 
		assertEquals(expectedWhere, query.getWhereClause());
	}

	@Test
	void testCreateAndQuery2() {
		QueryBuilderArgs args = createQueryExampleArgs();
		DbTable fromTable = args.getFrom().getFromTable();
		DbField f0 = fromTable.getFields().get(0);
		DbField f1 = fromTable.getFields().get(1);
		args.addConstraint(new StringConstraint(1, f0, StringOperatorEnum.BEGINS_WITH, "abc"));
		args.addConstraint(new StringConstraint(2, f0, StringOperatorEnum.ENDS_WITH, "cde"));
		args.addConstraint(new StringConstraint(3, f1, StringOperatorEnum.EQUALS, "xy"));

		QueryBuilder builder = new QueryBuilder();	
		SqlQuery query = builder.createAndQuery(args);
		System.out.println(query.getSql());
		
		String expectedWhere = String.format("%1$s like :p1 || '%%' and %1$s like '%%' || :p2 and %2$s = :p3", 
			f0.getFullName(), f1.getFullName()); 
		assertEquals(expectedWhere, query.getWhereClause());
	}
	
	@Test
	void testCreateOrQuery1() {
		QueryBuilderArgs args = createQueryExampleArgs();
		DbTable fromTable = args.getFrom().getFromTable();
		DbField f0 = fromTable.getFields().get(0);
		DbField f1 = fromTable.getFields().get(1);
		args.addConstraint(new StringConstraint(1, f0, StringOperatorEnum.BEGINS_WITH, "abc"));
		args.addConstraint(new StringConstraint(2, f0, StringOperatorEnum.ENDS_WITH, "cde"));
		args.addConstraint(new StringConstraint(3, f1, StringOperatorEnum.EQUALS, "xy"));
		
		QueryBuilder builder = new QueryBuilder();
		SqlQuery query = builder.createOrQuery(args);
		System.out.println(query.getSql());
		
		int pos1 = query.getSql().indexOf("\nunion", 0);
		int pos2 = query.getSql().indexOf("\nunion", pos1 + 1);
		assertTrue(pos1 > 0, "Union 1 found");
		assertTrue(pos2 > pos1, "Union 2 found");
	}

}
