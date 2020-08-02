package com.frog.graphql.test.querybuilder;

import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArgInfo;

import lombok.Data;
import lombok.Getter;

@Data
public class SqlQuery {
	@Getter private List<DbField> selectFieldList;
	@Getter private String sql;
	@Getter private JdbcArgInfo argInfo;	
}
