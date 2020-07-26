package com.frog.graphql.test.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.frog.graphql.test.jdbc.JdbcArg.VALUE_TYPE;

public class JdbcArgInfo {
	private ArrayList<JdbcArg> args;
	private ArrayList<String> stringArgs;
	private ArrayList<Double> numberArgs;
	
	public JdbcArgInfo() {
		args = new ArrayList<JdbcArg>();
		stringArgs = new ArrayList<String>();
		numberArgs = new ArrayList<Double>();
	}
	
	public List<String> getArgInfoList() {
		ArrayList<String> infoList = new ArrayList<String>();
		for (int i=0; i<args.size(); i++) {
			infoList.add(args.get(i).toString());
		}
		return infoList;
	}

	public ArrayList<String> getStringArgs() {
		return stringArgs;
	}

	public ArrayList<Double> getNumberArgs() {
		return numberArgs;
	}

	public void addStringArg(String argName, String argValue) {		
		JdbcArg jdbcArg = new JdbcArg();
		jdbcArg.setArgName(argName);
		jdbcArg.setValueCount(1);
		jdbcArg.setValueStartIndex(stringArgs.size() + 1);
		jdbcArg.setValueType(VALUE_TYPE.String);
		args.add(jdbcArg);
		
		stringArgs.add(argValue);
	}
	
	public void addDoubleArg(String argName, Double argValue) {
		JdbcArg jdbcArg = new JdbcArg();
		jdbcArg.setArgName(argName);
		jdbcArg.setValueCount(1);
		jdbcArg.setValueStartIndex(numberArgs.size() + 1);
		jdbcArg.setValueType(VALUE_TYPE.Number);
		args.add(jdbcArg);
		
		numberArgs.add(argValue);		
	}
	
	public void addLongArg(String argName, Long argValue) {
		addDoubleArg(argName, argValue.doubleValue());
	}

	public void addStringTableArg(String argName, List<String> argValueList) {		
		JdbcArg jdbcArg = new JdbcArg();
		jdbcArg.setArgName(argName);
		jdbcArg.setValueCount(argValueList.size());
		jdbcArg.setValueStartIndex(stringArgs.size() + 1);
		jdbcArg.setValueType(VALUE_TYPE.StringTable);
		args.add(jdbcArg);
		
		for (int i=0; i<argValueList.size(); i++) {
			stringArgs.add(argValueList.get(i));			
		}
	}

	private void addNumberTableArg(String argName, Object[] argValueList) {		
		JdbcArg jdbcArg = new JdbcArg();
		jdbcArg.setArgName(argName);
		jdbcArg.setValueCount(argValueList.length);
		jdbcArg.setValueStartIndex(numberArgs.size() + 1);
		jdbcArg.setValueType(VALUE_TYPE.NumberTable);
		args.add(jdbcArg);
		
		for (int i=0; i<argValueList.length; i++) {
			Object value = argValueList[i];
			if (value instanceof Double) {
				numberArgs.add((Double)value);				
			} else {
				numberArgs.add(((Long)value).doubleValue());
			}
		}
	}

	public void addDoubleTableArg(String argName, List<Double> argValue) {
		addNumberTableArg(argName, argValue.toArray());
	}
	
	public void addLongArg(String argName, List<Long> argValue) {		
		addNumberTableArg(argName, argValue.toArray());
	}
}
