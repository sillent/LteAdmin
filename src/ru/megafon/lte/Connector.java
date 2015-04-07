package ru.megafon.lte;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by santa on 30.03.15.
 */


public class Connector {
    public enum Type {
        MSAUTH,   // MSISDN into radchecks
        IPREPL,   // MSISDN&IP into radreplies
        ROUTREPL  // MSISDN&ROUTE into radreplies
    }

    private  String SQL_CHECK_MSISDN="select * from radchecks where username=?";
    private  String SQL_INS_MSISDN_AUTH="insert into radchecks (username,attrib,op,value) values (?,'clear',':=','password')";
    private  String SQL_INS_IP="insert into radreplies (username,attrib,op,value,created_at,updated_at) values (?,'Framed-IP-Address',':=',?,?,?)";
    private String SQL_INS_ROUTE = "insert into radreplies (username,attrib,op,value,created_at,updated_at) values (?,'Framed-Route','+=',?,?,?)";
    private String SQL_DEL_MSISDN_RADCHECK = "delete from radchecks where username=?";
    private String SQL_DEL_MSISDN_RADREPL = "delete from radreplies where username=?";
    private Connection connection;

    HashMap<String, String> map;
    Connector() {
        this("jdbc/myresource");
    }

    Connector(String name) {
        try {
            map = new HashMap<String, String>();
            InitialContext ctx = new InitialContext();
            DataSource dts = (DataSource) ctx.lookup(name);
            connection = dts.getConnection();
        } catch (NamingException nexcp) {
            nexcp.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkParamExist(String msisdn) throws SQLException {
        if (msisdn == null) {       // нет параметра msisdn
            return false;
        }
        if (msisdn.length() <= 0) {
            return false;
        }
        PreparedStatement stmt = connection.prepareStatement(SQL_CHECK_MSISDN);
        stmt.setString(1, msisdn);
        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next()) {
            stmt.close();
            return true;
        } else {
            stmt.close();
            return false;
        }
    }
    public void insertMsisdnInChecks(String msisdn) throws SQLException {
        map.put("msisdn", msisdn);
        System.out.println("map msisdn=" + map.get("msisdn"));
        insert(map, Type.MSAUTH);
    }

    public void insertIpInReplies(String msisdn, String ip) throws SQLException {
        map.put("msisdn", msisdn);
        map.put("ip", ip);
        insert(map, Type.IPREPL);
    }
    public void insertRouteInReplies(String msisdn, String route) throws SQLException {
        map.put("msisdn",msisdn);
        map.put("route", route);
        insert(map,Type.ROUTREPL);
    }

    public void deleteMsisdn(String msisdn) throws SQLException {
        PreparedStatement stmtc,stmtr;
        stmtc = connection.prepareStatement(SQL_DEL_MSISDN_RADCHECK);
        stmtr = connection.prepareStatement(SQL_DEL_MSISDN_RADREPL);
        stmtc.setString(1, msisdn);
        stmtr.setString(1, msisdn);
        stmtc.executeUpdate();
        stmtr.executeUpdate();
        stmtc.close();
        stmtr.close();
        return;
    }

    public void insert(HashMap<String, String> arg, Type type) {
        String msisdn=arg.get("msisdn");
        SimpleDateFormat smpdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String timeNow=smpdf.format(now);
        PreparedStatement stmt;
        if (type == Type.MSAUTH) {
            try {
                System.out.println("execute insert MSAUTH");
                stmt = connection.prepareStatement(SQL_INS_MSISDN_AUTH);
                stmt.setString(1, msisdn);
                System.out.println(stmt.toString());
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        if (type == Type.IPREPL) {
            String ip = arg.get("ip");
            try {
                stmt = connection.prepareStatement(SQL_INS_IP);
                stmt.setString(1, msisdn);
                stmt.setString(2, ip);
                stmt.setString(3, timeNow);
                stmt.setString(4, timeNow);
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        if (type == Type.ROUTREPL) {
            String route = arg.get("route");
            try {
                stmt = connection.prepareStatement(SQL_INS_ROUTE);
                stmt.setString(1, msisdn);
                stmt.setString(2, route);
                stmt.setString(3, timeNow);
                stmt.setString(4, timeNow);
                stmt.executeUpdate();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
