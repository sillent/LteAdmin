package ru.megafon.lte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by santa on 30.03.15.
 */
public class Connector {

    Connection connection;
    private static String driver = "com.mysql.jdbc.Driver";
    private static String jdbc="jdbc:mysql";
    private static String hostName = "127.0.0.1";
    private static String osid = "radius";
    private static String nam = "root";
    private static String pas = "root";
    private static int portNo = 3306;
    boolean connected=false;

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    Connector() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) { e.printStackTrace(); }
        try {
            if (!isConnected()) {
                connection = DriverManager.getConnection(jdbc + "://" + hostName+":"+portNo+"/" + osid, nam, pas);
                setConnected(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setConnected(false);
        }
    }

    public Connection getConnection() {
        if(isConnected()) {
            return connection;
        } else
            return null;
    }
    // @close connection
    public void closeConnection() {
        try {
            if (isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("cannot close connection");
        }
    }
}
