package com.frog.graphql.test.querybuilder.constraint;

import java.util.ArrayList;
import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.querybuilder.DbField;

import lombok.Data;

@Data
public class NuericConstraint extends QueryConstraint {
	private String sqlExpression;
	private DbField field;
	private NumericOperatorEnum operator;
	private List<Double> values;
	
	public NuericConstraint(int oneBasedConstraintIndex, DbField field, NumericOperatorEnum operator, List<Double> values) {
		super(oneBasedConstraintIndex);
		this.field = field;
		this.operator = operator;
		this.values = values;
	}

	public NuericConstraint(int oneBasedConstraintIndex, DbField field, NumericOperatorEnum operator, Double value) {
		this(oneBasedConstraintIndex, field, operator, new ArrayList<Double>());
		values.add(value);
	}
	
	@Override
	public String getSqlClause() {
		if (operator == NumericOperatorEnum.SQL_EXPRESSION) {
			return sqlExpression;	
		} else if (operator == NumericOperatorEnum.EQUALS) {
			return String.format("%s = :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == NumericOperatorEnum.GREATER) {
			return String.format("%s > :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == NumericOperatorEnum.LESS) {
			return String.format("%s < :p%d", field.getFullName(), getConstraintIndex());	
		} else if (operator == NumericOperatorEnum.BETWEEN) {
			return String.format("%s between :p%d-v1 and :p%d-v2", field.getFullName(), getConstraintIndex(), getConstraintIndex());	
		} else if (operator == NumericOperatorEnum.IN) {
			return String.format("%s in (select column_value from table(:p%d))", field.getFullName(), getConstraintIndex());
		}
		return null;
	}
	
	@Override
	public void addArgsTo(JdbcArgInfo argInfo) {
		if (operator == NumericOperatorEnum.SQL_EXPRESSION) {
			return;
		} else if (operator == NumericOperatorEnum.EQUALS) {
			argInfo.addDoubleArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == NumericOperatorEnum.GREATER) {
			argInfo.addDoubleArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == NumericOperatorEnum.LESS) {
			argInfo.addDoubleArg(field.getFullName(), values.get(0));
			return;
		} else if (operator == NumericOperatorEnum.BETWEEN) {
			argInfo.addDoubleBetweenArg(field.getFullName(), values.get(0), values.get(1));
			return;
		} else if (operator == NumericOperatorEnum.IN) {
			argInfo.addDoubleTableArg(field.getFullName(), values);
			return;
		}
	}
}
