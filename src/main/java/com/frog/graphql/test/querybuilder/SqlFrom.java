package com.frog.graphql.test.querybuilder;

import java.util.List;

import lombok.Data;

@Data
public class SqlFrom {
	private DbTable fromTable;
	private List<SqlJoin> joinList;
}
