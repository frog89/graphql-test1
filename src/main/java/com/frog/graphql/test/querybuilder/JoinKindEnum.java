package com.frog.graphql.test.querybuilder;

public enum JoinKindEnum {
	INNER("INNER JOIN"),
	LEFT("LEFT JOIN"),
	RIGHT("RIGHT JOIN");
	
	private String sqlClause;
	
	JoinKindEnum(String sqlClause) {
		this.sqlClause = sqlClause;
	}
	
	@Override
	public String toString() {
		return this.sqlClause;
	}
}
