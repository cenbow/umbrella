package com.harmony.umbrella.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author wuxii@foxmail.com
 */
public class JdbcConnectionSource implements ConnectionSource {

    private final String url;

    private int timeout;

    private final Properties props = new Properties();

    public JdbcConnectionSource(String url, String user, String password) {
        this(url, user, password, new Properties());
    }

    public JdbcConnectionSource(String url, String user, String password, Properties props) {
        this.url = url;
        this.props.putAll(props);
        this.props.put("user", user);
        this.props.put("password", password);
    }

    @Override
    public boolean isValid() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn.isValid(timeout);
        } catch (SQLException e) {
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, props);
    }

}