package com.frog.graphql.test.querybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphql.schema.GraphQLList;
import graphql.schema.SelectedField;
import lombok.Data;
import lombok.Getter;

@Data
public class DbTable {
	private final String id;
	private final String dbExpression;
	private final String dbAlias;
	@Getter private List<DbField> keyFields;
	@Getter private List<DbField> fields;
	
	public DbTable(String id, String dbExpression, String dbAlias) {
		keyFields = new ArrayList<DbField>();
		fields = new ArrayList<DbField>();		
		this.id = id;
		this.dbExpression = dbExpression;
		this.dbAlias = dbAlias;
	}
	
	public void addKeyField(DbField keyField) {
		keyFields.add(keyField);
	}
	
	public void addField(DbField field) {
		fields.add(field);
	}
}
