package com.frog.graphql.test.querybuilder;

import java.util.ArrayList;
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

	public List<DbField> findFields(List<SelectedField> selectedGraphQlFields) {
		List<DbField> selectFieldList = new ArrayList<DbField>();
		for (SelectedField graphQlField : selectedGraphQlFields) {
			if (graphQlField.getFieldDefinition().getType() instanceof GraphQLList) {
				continue;
			}
			for (DbField field : fields) {
				if (field.getGraphQlAlias().equals(graphQlField.getName())) {
					selectFieldList.add(field);
					break;
				}
			}
		}
		for (DbField keyField : keyFields) {
			if (!selectFieldList.contains(keyField)) {
				selectFieldList.add(keyField);
			}
		}
		return selectFieldList;
	}
}
