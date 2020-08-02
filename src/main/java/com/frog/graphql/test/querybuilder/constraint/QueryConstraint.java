package com.frog.graphql.test.querybuilder.constraint;

import com.frog.graphql.test.jdbc.JdbcArgInfo;

import lombok.Data;

@Data
public abstract class QueryConstraint {
	private int constraintIndex; // Has to start with index 1
	
	public QueryConstraint(int oneBasedConstraintIndex) {
		this.constraintIndex = oneBasedConstraintIndex;
	}
	
	public abstract String getSqlClause();

	public abstract void addArgsTo(JdbcArgInfo argInfo);
}
