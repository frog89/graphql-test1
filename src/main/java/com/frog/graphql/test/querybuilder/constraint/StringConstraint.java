package com.frog.graphql.test.querybuilder.constraint;

import java.util.ArrayList;
import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.querybuilder.DbField;

import lombok.Data;

@Data
public class StringConstraint extends QueryConstraint {
	private String sqlExpression;
	private DbField field;
	private StringOperatorEnum operator;
	private List<String> values;

	public StringConstraint(int oneBasedConstraintIndex, DbField field, StringOperatorEnum operator, List<String> values) {
		super(oneBasedConstraintIndex);
		this.field = field;
		this.operator = operator;
		this.values = values;
	}

	public StringConstraint(int oneBasedConstraintIndex, DbField field, StringOperatorEnum operator, String value) {
		this(oneBasedConstraintIndex,field, operator, new ArrayList<String>());
		values.add(value);
	}
	
	@Override
	public String getSqlClause() {
		if (operator == StringOperatorEnum.SQL_EXPRESSION) {
			return sqlExpression;	
		} else if (operator == StringOperatorEnum.BEGINS_WITH) {
			return String.format("%s like :p%d || '%%'", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.ENDS_WITH) {
			return String.format("%s like '%%' || :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.CONTAINS) {
			return String.format("%s like '%%' || :p%d || '%%'", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.EQUALS) {
			return String.format("%s = :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.GREATER) {
			return String.format("%s > :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.LESS) {
			return String.format("%s < :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.BETWEEN) {
			return String.format("%s between :p%d_v1 and :p%d_v2", field.getFullName(), getConstraintIndex());	
		} else if (operator == StringOperatorEnum.IN) {
			return String.format("%s in (select column_value from table(:p%d))", field.getFullName(), getConstraintIndex());
		}
		return null;
	}
	
	@Override
	public void addArgsTo(JdbcArgInfo argInfo) {
		if (operator == StringOperatorEnum.SQL_EXPRESSION) {
			return;
		} else if (operator == StringOperatorEnum.BEGINS_WITH) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.ENDS_WITH) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.CONTAINS) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.EQUALS) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.GREATER) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.LESS) {
			argInfo.addStringArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == StringOperatorEnum.BETWEEN) {
			argInfo.addStringBetweenArg(field.getFullName(), values.get(0), values.get(1));
			return;
		} else if (operator == StringOperatorEnum.IN) {
			argInfo.addStringTableArg(field.getFullName(), values);
			return;
		}
	}

}
