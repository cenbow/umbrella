package com.harmony.umbrella.context.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.harmony.umbrella.core.ConnectionSource;

/**
 * 应用所使用的数据库信息
 * <table border="2" rules="all" cellpadding="4">
 * <thead>
 * <tr>
 * <th align="center" colspan="5">数据库类型对照表</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <th>Database</th>
 * <th>Database Type</th>
 * <th>Database Name</th>
 * </tr>
 * <tr>
 * <td>Oracle</td>
 * <td>1</td>
 * <td>oracle</td>
 * </tr>
 * <tr>
 * <td>MySQL</td>
 * <td>2</td>
 * <td>mysql</td>
 * </tr>
 * <tr>
 * <td>DB2</td>
 * <td>3</td>
 * <td>db2</td>
 * </tr>
 * <tr>
 * <td>H2</td>
 * <td>4</td>
 * <td>h2</td>
 * </tr>
 * <tr>
 * <td>HSQL</td>
 * <td>5</td>
 * <td>hsql</td>
 * </tr>
 * <tr>
 * <td>SQLServer</td>
 * <td>6</td>
 * <td>sqlserver</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>POSTGRESQL</td>
 * <td>7</td>
 * <td>postgresql</td>
 * </tr>
 * <tr>
 * <td>其他</td>
 * <td>0</td>
 * <td>unknow</td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * @author wuxii@foxmail.com
 */
public final class DatabaseMetadata {

    static final DatabaseMetadata EMPTY_DATABASE_METADATA = new DatabaseMetadata();

    /**
     * 数据库的名称
     */
    public final String productName;
    /**
     * 数据库的版本
     */
    public final String productVersion;
    /**
     * 数据库的url
     */
    public final String url;
    /**
     * 数据库的用户名
     */
    public final String userName;
    /**
     * 驱动名称
     */
    public final String driverName;
    /**
     * 驱动版本
     */
    public final String driverVersion;
    /**
     * 数据库类型
     */
    public final int databaseType;

    private ConnectionSource connectionSource;

    public static final int UNKNOW = 0;
    public static final int ORACLE = 1;
    public static final int MYSQL = 2;
    public static final int DB2 = 3;
    public static final int H2 = 4;
    public static final int HSQL = 5;
    public static final int SQLSERVER = 6;
    public static final int POSTGRESQL = 7;

    public DatabaseMetadata(ConnectionSource connectionSource) throws SQLException {
        this.connectionSource = connectionSource;
        Connection conn = null;
        try {
            conn = connectionSource.getConnection();
            DatabaseMetaData dbmd = conn.getMetaData();
            this.productName = dbmd.getDatabaseProductName();
            this.productVersion = dbmd.getDatabaseProductVersion();
            this.url = dbmd.getURL();
            this.userName = dbmd.getUserName();
            this.driverName = dbmd.getDriverVersion();
            this.driverVersion = dbmd.getDriverVersion();
            this.databaseType = databaseType(productName);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private DatabaseMetadata() {
        this.productName = "";
        this.productVersion = "";
        this.url = "";
        this.userName = "";
        this.driverName = "";
        this.driverVersion = "";
        this.databaseType = UNKNOW;
    }

    private final int databaseType(String databaseName) {
        if (databaseName == null) {
            return UNKNOW;
        }
        databaseName = databaseName.toLowerCase();
        if (databaseName.indexOf("oracle") != -1) {
            return ORACLE;
        } else if (databaseName.indexOf("postgresql") != -1) {
            return POSTGRESQL;
        } else if (databaseName.indexOf("db2") != -1) {
            return DB2;
        } else if (databaseName.indexOf("sql server") != -1) {
            return SQLSERVER;
        } else if (databaseName.indexOf("mysql") != -1) {
            return MYSQL;
        } else if (databaseName.indexOf("hsql") != -1) {
            return HSQL;
        } else if (databaseName.indexOf("h2") != -1) {
            return H2;
        }
        return UNKNOW;
    }

    public Connection getConnection() throws SQLException {
        return connectionSource == null ? null : connectionSource.getConnection();
    }

}