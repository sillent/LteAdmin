package ru.megafon.lte;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


/**
 * Created by Dmitry Ulyanov on 30.03.15.
 * Senjor Manager
 */
public class Add extends HttpServlet {
    Connector connector;
    private String Ip="";       // \
    private String Msisdn="";   //  | -инициализируем пустыми строками чтобы работали проверки .length()
    private String Route="";    // /
//    private boolean exist=false;  // для проверки что уже номер заведен в БД
    private final String SQL_CHECK_MSISDN="select * from radchecks where username=?";
    private final String SQL_INS_MSISDN_AUTH="insert into radchecks (username,attrib,op,value) values (?,'clear',':=','password')";
    private final String SQL_INS_IP="insert into radreplies (username,attrib,op,value,created_at,updated_at) values (?,'Framed-IP-Address',':=',?,?,?)";
    private final String SQL_IN_ROUTE="insert into radreplies (username,attrib,op,value,created_at,updated_at) values (?,'Framed-Route','+=',?,?,?)";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean exist=false;
        Enumeration<String> en = req.getParameterNames();
        Connection connection;
        PrintWriter pw = resp.getWriter();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date();
        String dateNow=sdf.format(date);
        while (en.hasMoreElements()) {
            String s = en.nextElement();
            if (s.compareTo("ip") == 0) {
                setIp(req.getParameter("ip"));
            }
            if (s.compareTo("route") == 0) {
                setRoute(req.getParameter("route"));
            }
            if (s.compareTo("msisdn") == 0) {
                setMsisdn(req.getParameter("msisdn"));
            }
        }
        if (getMsisdn().length() > 0 && (getIp().length() > 0 || getRoute().length() > 0)) {
            // Проверяем что номер не заведен в БД
            try {
                InitialContext ctx = new InitialContext();
                DataSource ds = (DataSource) ctx.lookup("jdbc/myresource");
                connection=ds.getConnection();
                try {
                    PreparedStatement stmt = connection.prepareStatement(SQL_CHECK_MSISDN);
                    stmt.setString(1, getMsisdn());
                    ResultSet resultSet = stmt.executeQuery();
                    if (resultSet.next()) {
                        exist = true;
                    }
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (exist && getRoute().length() == 0) {    // если обычное добавление связки MSISDN-IP при существующей связке
                    printResponse(pw, 600, null);
                    connection.close();
                    return;
                }
                if (exist && getRoute().length() > 0 && getIp().length() > 0) {  // msisdn=<yes>&ip=&route=
                    try {
                        PreparedStatement stmt = connection.prepareStatement(SQL_CHECK_MSISDN);
                        stmt.setString(1, getMsisdn());
                        ResultSet resultSet = stmt.executeQuery();
                        if (resultSet.next()) {
                            String msisdnE = resultSet.getString(2);
                            printResponse(pw, 600, msisdnE);
                        }
                        stmt.close();
                        connection.close();
                        return;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (!exist && getRoute().length() == 0 && getIp().length() > 0) {  // msisdn=<no>&ip= - добавить в обе таблицы
                    PreparedStatement stmtauth = connection.prepareStatement(SQL_INS_MSISDN_AUTH);
                    PreparedStatement stmt = connection.prepareStatement(SQL_INS_IP);
                    stmtauth.setString(1,getMsisdn());
                    stmt.setString(1, getMsisdn());
                    stmt.setString(2, getIp());
                    stmt.setString(3, dateNow);
                    stmt.setString(4, dateNow);
                    stmtauth.executeUpdate();
                    stmt.executeUpdate();
                    stmtauth.close();
                    stmt.close();
                    connection.close();
                    printResponse(pw,200,null);
                    return;
                }
                if (exist && getRoute().length() > 0 && getIp().length() == 0) {   // msisdn=<yes>&route= - добавить
                    PreparedStatement stmtRoute = connection.prepareStatement(SQL_IN_ROUTE);
                    stmtRoute.setString(1, getMsisdn());
                    stmtRoute.setString(2, getRoute());
                    stmtRoute.setString(3, dateNow);
                    stmtRoute.setString(4, dateNow);
                    stmtRoute.executeUpdate();
                    stmtRoute.close();
                    connection.close();
                    printResponse(pw,200,null);
                    return;
                }
                if (!exist && getRoute().length() > 0 && getIp().length() == 0) {
                    printResponse(pw,600,null);
                }
            } catch (NamingException ne) {
                ne.printStackTrace();
            } catch (SQLException sqle) {
                printResponse(pw,666,null);
            }
        }
    }

    private Connection initConnection() {
        connector=new Connector();
        Connection connection = connector.getConnection();
        if (connection == null) {
            return null;
        }
        return connection;
    }
    private void closeConnection() {
        connector.closeConnection();
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public String getMsisdn() {
        return Msisdn;
    }

    public void setMsisdn(String msisdn) {
        Msisdn = msisdn;
    }

    public String getRoute() {
        return Route;
    }

    public void setRoute(String route) {
        Route = route;
    }

    public void printResponse(PrintWriter pw, int code, String msisdn_exist) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<result>");
        pw.println("<code>" + code + "</code>");
        if (msisdn_exist != null) {
            pw.println("<msisdn>" + msisdn_exist + "</msisdn>");
        }
        pw.println("</result>");

    }
}
