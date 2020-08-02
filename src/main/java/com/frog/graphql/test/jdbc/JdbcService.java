package com.frog.graphql.test.jdbc;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import lombok.Data;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;

@Service
@Data
@ConfigurationProperties(prefix = "ora12pdb")
public class JdbcService {
	private OracleDataSource dataSource;
	
	private String url; // "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + database
	private String username;
	private String password;
  
	private void initDataSource() {
		if (dataSource != null) {
			return;
		}
			
		OracleConnectionPoolDataSource opds;
		try {
			opds = new OracleConnectionPoolDataSource();
			opds.setURL(url);
			opds.setUser(username);
			opds.setPassword(password);
			dataSource = opds; 
		} catch (SQLException e1) {
			System.err.println("Connection failed!");
		}
		try {
			// Load driver
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Driver not found!");
		}
	}
  
	public Connection getConnection() throws SQLException {
		initDataSource();
		return dataSource.getConnection();
	}
	
	public void consumeData(String sql, JdbcArgInfo argInfo, Consumer<ResultSet> consumer) throws Exception {
		long startTime = System.nanoTime();
		doConsumeData(sql, argInfo, consumer);
		long timeInMillis = (System.nanoTime() - startTime) / 1000000;
		String msg = String.format("Time for sql %s was %d ms.", sql, timeInMillis);
		System.out.println(msg);
	}
	
	public void doConsumeData(String sql, JdbcArgInfo argInfo, Consumer<ResultSet> consumer) throws Exception {
		String method ="{? = call get_data_pkg.get_data(?,?,?,?)}";
		OracleConnection conn = null;
		CallableStatement statement = null;
		ResultSet resultSet = null; 
		try {
			conn = (OracleConnection)this.getConnection();
			statement = conn.prepareCall(method);
			statement.registerOutParameter(1, Types.REF_CURSOR);
			
			Clob clob1 = conn.createClob();
			clob1.setString(1, sql);
			statement.setClob(2, clob1);
			
//			Clob clob2 = conn.createClob();
//			clob2.setString(1, jsonArgs);
//			statement.setClob(3, clob2);

			List<String> argInfoList = null;
			List<Double> argNumberList = null;
			List<String> argStringList = null;
			if (argInfo == null) {
				argInfoList = new ArrayList<String>();				
				argNumberList = new ArrayList<Double>();
				argStringList = new ArrayList<String>();
			} else {
				argInfoList = argInfo.getArgInfoList();
				argNumberList = argInfo.getNumberArgs();
				argStringList = argInfo.getStringArgs();
			}
			Array argArray = conn.createOracleArray("HR2.VARCHAR2_200_TABLE", argInfoList.toArray(new String[0]));
			statement.setArray(3, argArray);

			Array numberListArray = conn.createOracleArray("HR2.NUMBER_TABLE", argNumberList.toArray(new Double[0]));
			statement.setArray(4, numberListArray);

			Array stringListArray = conn.createOracleArray("HR2.VARCHAR2_200_TABLE", argStringList.toArray(new String[0]));
			statement.setArray(5, stringListArray);

			statement.execute();
			resultSet = statement.getObject(1, ResultSet.class); 

			while (resultSet.next()) {
				consumer.accept(resultSet);
			}
		} catch(Exception ex) {
			System.out.println(ex);
			throw new Exception("Error in consumeData: " + ex.getMessage());
		} finally {
			if (resultSet != null) { try { resultSet.close(); } catch (Exception ex) { /* ignore */ }}
			if (statement != null) { try { statement.close(); } catch (Exception ex) { /* ignore */ }}
			if (conn != null) { try { conn.close(); } catch (Exception ex) { /* ignore */ }}
		}
	}
}
