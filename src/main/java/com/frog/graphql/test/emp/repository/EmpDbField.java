package com.frog.graphql.test.emp.repository;

import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.DbTable;
import com.frog.graphql.test.querybuilder.FieldTypeEnum;

public class EmpDbField extends DbField {
	public EmpDbField(EmpFieldEnum empFieldEnum, String fieldName, FieldTypeEnum fieldTypeEnum) {
		super(EmpFieldEnum.toOrdinalString(empFieldEnum), fieldName, fieldTypeEnum);
	}
	
	public EmpDbField(EmpFieldEnum empFieldEnum, String fieldName, FieldTypeEnum fieldTypeEnum, 
			String fieldAlias, DbTable table) {
		super(EmpFieldEnum.toOrdinalString(empFieldEnum), fieldName, fieldTypeEnum, fieldAlias, table);
	}

	public EmpFieldEnum getEmpFieldEnum() {
		return EmpFieldEnum.fromOrdinalString(this.getId());
	}
}
