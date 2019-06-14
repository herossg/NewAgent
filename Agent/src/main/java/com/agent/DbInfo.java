package com.agent;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

public class DbInfo {
	public static String DBMS;
	public static String SID;
	public static String MSG_TABLE;
	public static String BROADCAST_TABLE;
	public static HikariDataSource dbSource;
}
