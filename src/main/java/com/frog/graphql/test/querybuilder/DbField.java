package com.frog.graphql.test.querybuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class DbField {
	private final String id; 
	private final String graphQlAlias;
	private final String fieldName;
	private final FieldTypeEnum fieldTypeEnum;
	private String dbAlias;
	private DbTable table;
	
	public String getFullName() {
		return String.format("%s.%s", getTable().getDbAlias(), fieldName);
	}
	
	@Override
	public boolean equals(Object otherFieldObj) {
		DbField otherField = (DbField)otherFieldObj;
		if (otherField == null) {
			return false;			
		}
		return getId().equals(otherField.getId());
	}
}
