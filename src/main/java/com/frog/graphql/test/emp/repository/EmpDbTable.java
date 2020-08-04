package com.frog.graphql.test.emp.repository;

import com.frog.graphql.test.querybuilder.DbTable;

public class EmpDbTable extends DbTable {
	public EmpDbTable(EmpTableEnum empTableEnum, String dbExpression, String dbAlias) {
		super(EmpTableEnum.toOrdinalString(empTableEnum), dbExpression, dbAlias);
	}
	
	public EmpTableEnum getEmpTableEnum() {
		return EmpTableEnum.fromOrdinalString(this.getId());
	}
}
