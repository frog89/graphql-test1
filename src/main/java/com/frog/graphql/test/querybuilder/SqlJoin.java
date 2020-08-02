package com.frog.graphql.test.querybuilder;

import lombok.Data;

@Data
public class SqlJoin {
	private String joinDbExpression;
	
	private JoinKindEnum joinKind;
	private DbTable joinTable;
	private DbTable joinToTable;

	public SqlJoin(JoinKindEnum joinKind, DbTable joinTable, DbTable joinToTable) {
		this.joinKind = joinKind;
		this.joinTable = joinTable;
		this.joinToTable = joinToTable;
	}
			
	public String getJoinClause() {
		if (joinDbExpression != null) {
			return joinDbExpression;
		}
		StringBuffer keys = new StringBuffer();
		for (int i=0; i<joinTable.getKeyFields().size(); i++) {
			DbField f = joinTable.getKeyFields().get(i);
			if (keys.length() > 0) {
				keys.append(" and ");
			}
			keys.append(String.format("%s.%s = %s.%s", 
				joinTable.getDbAlias(), f.getFieldName(), 
				joinToTable.getDbAlias(), f.getFieldName()));
		}
		
		return String.format("%s %s %s on %s", joinKind, joinTable.getDbExpression(), joinTable.getDbAlias(), keys.toString());
	}
}
