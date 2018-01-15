package com.simple;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.driver.OracleDriver;

public class ODB extends OracleDriver {
	private static String HOST = "127.0.0.1";
	private static String PORT = "1521";
	private static String SID = "orcl";
	private static String USERNAME = "mike";
	private static String PASSWORD = "mike";
	
	private static boolean sset = false;
	
	private static String url = "";
	
	public static void setting(){
		if(sset) return ;
		try {
			
			url = "jdbc:oracle:thin:@"+ HOST + ":" + PORT + ":" + SID;
			sset = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Connection createConn() {
		setting();
		Properties props = new Properties(); 
		props.put("user", USERNAME); 
		props.put("password", PASSWORD); 
		props.put("internal_logon", SID);
		props.put("MaxLimit", "99999");
		
		Connection conn = null;
		try {
			//conn = DriverManager.getConnection(url, ODB.USERNAME, ODB.PASSWORD);
			conn = DriverManager.getConnection(url, props);
		} catch (SQLException e) {}
		return conn;
	}
	public static PreparedStatement prepare(Connection conn, String sql) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ps;
	}
	
	public static void close (Connection conn) {
		if (conn==null) return ;
		try {
			conn.close();
		} catch (SQLException e) {
		}
	}
	
	public static void close (Statement stmt) {
		if (stmt==null) return ;
		try {
			stmt.close();
		} catch (SQLException e) {
		}
	}
	
	public static void close (ResultSet rs) {
		if (rs==null) return ;
		try {
			rs.close();
		} catch (SQLException e) {
		}
	}
	
	public static void close (Object ...os) {
		for(Object o:os) {
			if(o instanceof Connection) {
				try {((Connection)o).close();} catch (SQLException e) {}
			} else if(o instanceof Statement) {
				try {((Statement)o).close();} catch (SQLException e) {}
			} else if(o instanceof ResultSet) {
				try {((ResultSet)o).close();} catch (SQLException e) {}
			} else {
				System.out.println("close error");
			}
		}
	}
	
	
	public static String makeOrList(String field, String[] str) {
		return makeList("OR", field, str);
	}
	
	public static String makeAndList(String field, String[] str) {
		return makeList("AND", field, str);
	} 
	
	public static String makeList(String symbol, String field, String[] str) {
		String sum = "";
		for(int i=0; i<str.length; i++) {
			if(i==0) {
				sum = field + " = ? ";
			} else {
				sum+= symbol+ " " + field + " = ? ";
			}
		}
		return sum;
	}
}
