package com.frog.graphql.test.querybuilder;

import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArgInfo;

import lombok.Data;

@Data
public class SqlQuery {
	private List<DbField> selectFieldList;
	private SqlFrom from;
	private String fieldClause;
	private String fromClause;
	private String whereClause;
	private String pageClause;
	private String sql;
	private JdbcArgInfo argInfo;	
}
