package com.frog.graphql.test.querybuilder;

import com.frog.graphql.test.jdbc.JdbcArgInfo;
import com.frog.graphql.test.querybuilder.constraint.QueryConstraint;

public class QueryBuilder {
	private String createFieldClause(QueryBuilderArgs args) {
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
		
		return fields.toString();
	}

	private String createFromClause(QueryBuilderArgs args) {
		StringBuffer joinBuffer = new StringBuffer();
		
		if (args.getFrom().getJoinList() != null) {
			if (joinBuffer.length() > 0) {
				joinBuffer.append("\n");
			}
			for (SqlJoin join : args.getFrom().getJoinList()) {
				joinBuffer.append(join.getJoinClause());
			}			
		}
		
		StringBuffer fromBuffer = new StringBuffer(); 
		
		fromBuffer.append(String.format("%s %s", 
			args.getFrom().getFromTable().getDbExpression(), 
			args.getFrom().getFromTable().getDbAlias()));
		if (joinBuffer.length() > 0) {
			fromBuffer.append("\n");
			fromBuffer.append(joinBuffer);
		}
		
		return fromBuffer.toString();
	}

	private String createWhereClause(QueryBuilderArgs args) {
		StringBuffer whereBuffer = new StringBuffer();

		for (QueryConstraint constraint : args.getConstraintList()) {
			if (whereBuffer.length() > 0) {
				whereBuffer.append(" and ");
			}
			whereBuffer.append(constraint.getSqlClause());
		}
		
		return whereBuffer.toString();
	}
	
	private String createPageClause(QueryBuilderArgs args) {
		StringBuffer pageBuffer = new StringBuffer();
		if (args.getExecutionParameters().getPageOffsetRowCount() > 0) {
			pageBuffer.append(String.format(" offset %d rows", args.getExecutionParameters().getPageOffsetRowCount()));
		}
		if (args.getExecutionParameters().getPageRowCount() > 0) {
			pageBuffer.append(String.format(" fetch next %d rows only", args.getExecutionParameters().getPageRowCount()));
		}
		return pageBuffer.toString();
	}
	
	private String createSql(String fieldClause, String fromClause, String whereClause, String pageClause) {
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append(fieldClause);
		sql.append("\nfrom ");
		sql.append(fromClause);
		sql.append("\nwhere ");
		sql.append(whereClause);
		if (pageClause.length() > 0) {
			sql.append("\n");
			sql.append(pageClause);			
		}
		return sql.toString();
	}
	
	public SqlQuery createQuery(QueryBuilderArgs args) {
		String fieldClause = createFieldClause(args);
		String fromClause = createFromClause(args);
		String pageClause = createPageClause(args);
		String whereClause = createWhereClause(args);
		
		JdbcArgInfo argInfo = new JdbcArgInfo();
		for (QueryConstraint constraint : args.getConstraintList()) {
			constraint.addArgsTo(argInfo);
		}
		
		String sql = createSql(fieldClause, fromClause, whereClause, pageClause);
				
		SqlQuery sqlQuery = new SqlQuery();
		sqlQuery.setSelectFieldList(args.getSelectFieldList());
		sqlQuery.setFrom(args.getFrom());
		sqlQuery.setFieldClause(fieldClause);
		sqlQuery.setFromClause(fromClause);
		sqlQuery.setWhereClause(whereClause);
		sqlQuery.setPageClause(pageClause);
		sqlQuery.setSql(sql.toString());
		sqlQuery.setArgInfo(argInfo);
		return sqlQuery;
	}
}
