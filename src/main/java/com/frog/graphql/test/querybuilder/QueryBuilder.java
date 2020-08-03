package com.frog.graphql.test.querybuilder;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;

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

	public SqlQuery createQuery(QueryBuilderArgs args) {
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		
		StringBuffer fields = new StringBuffer();
		for (DbField field : args.getSelectFieldList()) {
			if (fields.length() > 0) {
				fields.append(", ");
			}
			if (field.getFieldAlias() == null) {
				fields.append(field.getFullName());
			} else {				
				fields.append(String.format("%s as %s", field.getFieldName(), field.getFieldAlias()));
			}
		}
		
		sql.append(fields.toString());
		sql.append("\n");
		
		StringBuffer joinBuffer = new StringBuffer();
		joinBuffer.append(String.format("from %s %s\n", 
			args.getFrom().getFromTable().getDbExpression(), 
			args.getFrom().getFromTable().getDbAlias()));
		if (args.getFrom().getJoinList() != null) {
			for (SqlJoin join : args.getFrom().getJoinList()) {
				joinBuffer.append(String.format("%s\n", join.getJoinClause()));
			}			
		}
		sql.append(joinBuffer.toString());
		
		JdbcArgInfo argInfo = new JdbcArgInfo();
		if (args.getConstraintList() != null && args.getConstraintList().size() > 0) {
			StringBuffer whereBuffer = new StringBuffer();
			for (QueryConstraint constraint : args.getConstraintList()) {
				if (whereBuffer.length() > 0) {
					whereBuffer.append(String.format(" %s ", args.getSqlOperator().name()));
				}
				whereBuffer.append(constraint.getSqlClause());
				constraint.addArgsTo(argInfo);
			}
			sql.append("where ");
			sql.append(whereBuffer.toString());			
		}
		if (args.getExecutionParameters().getPageOffsetRowCount() > 0) {
			sql.append(String.format(" offset %d rows", args.getExecutionParameters().getPageOffsetRowCount()));
		}
		if (args.getExecutionParameters().getPageRowCount() > 0) {
			sql.append(String.format(" fetch next %d rows only", args.getExecutionParameters().getPageRowCount()));
		}
		
		SqlQuery sqlQuery = new SqlQuery();
		sqlQuery.setSelectFieldList(args.getSelectFieldList());
		sqlQuery.setSql(sql.toString());
		sqlQuery.setArgInfo(argInfo);
		return sqlQuery;
	}
}
