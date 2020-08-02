package com.frog.graphql.test.querybuilder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorGroup;
import com.frog.graphql.test.util.Node;

public class QueryBuilder {
//	public SqlQuery createQuery(List<DbField> selectFieldList, List<SqlJoin> joinList, 
//			List<QueryConstraint> constraintList, 
//			Node<SqlOperatorGroup> sqlOperatorGroup) {
//
//		Hashtable<Integer, List<QueryConstraint>> groupIndexTable = new Hashtable<Integer, List<QueryConstraint>>(); 
//		for (QueryConstraint constraint : constraintList) {
//			List<QueryConstraint> groupConstraints = null;
//			if (groupIndexTable.containsKey(constraint.getOperatorGroupIndex())) {
//				groupConstraints = groupIndexTable.get(constraint.getOperatorGroupIndex()); 
//			} else {
//				groupConstraints = new ArrayList<QueryConstraint>();
//			}
//			groupConstraints.add(constraint);
//		}
//		
//		return null;
//	}

	public SqlQuery createQuery(List<DbField> selectFieldList, SqlFrom from, 
			List<QueryConstraint> constraintList, SqlOperatorEnum sqlOperator) {
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		
		StringBuffer fields = new StringBuffer();
		for (DbField field : selectFieldList) {
			if (fields.length() > 0) {
				fields.append(", ");
			}
			if (field.getDbAlias() == null) {
				fields.append(field.getFullName());
			} else {				
				fields.append(String.format("%s as %s", field.getFieldName(), field.getDbAlias()));
			}
		}
		
		sql.append(fields.toString());
		sql.append("\n");
		
		StringBuffer joinBuffer = new StringBuffer();
		joinBuffer.append(String.format("from %s %s\n", from.getFromTable().getDbExpression(), from.getFromTable().getDbAlias()));
		if (from.getJoinList() != null) {
			for (SqlJoin join : from.getJoinList()) {
				joinBuffer.append(String.format("%s\n", join.getJoinClause()));
			}			
		}
		sql.append(joinBuffer.toString());
		
		JdbcArgInfo argInfo = new JdbcArgInfo();
		if (constraintList != null && constraintList.size() > 0) {
			StringBuffer whereBuffer = new StringBuffer();
			for (QueryConstraint constraint : constraintList) {
				if (whereBuffer.length() > 0) {
					whereBuffer.append(String.format(" %s ", sqlOperator.name()));
				}
				whereBuffer.append(constraint.getSqlClause());
				constraint.addArgsTo(argInfo);
			}
			sql.append("where ");
			sql.append(whereBuffer.toString());			
		}
		
		SqlQuery sqlQuery = new SqlQuery();
		sqlQuery.setSelectFieldList(selectFieldList);
		sqlQuery.setSql(sql.toString());
		sqlQuery.setArgInfo(argInfo);
		return sqlQuery;
	}
}
