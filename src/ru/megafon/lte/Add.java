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
import java.util.Enumeration;


/**
 * Created by Dmitry Ulyanov on 30.03.15.
 * Senjor Manager
 */
public class Add extends HttpServlet {
    Connector connector;
    private String Ip="";       // \
    private String Msisdn="";   //  | -инициализируем пустыми строками чтобы работали проверки .length()
    private String Route="";    // /
    private boolean exist=false;  // для проверки что уже номер заведен в БД
    private final String SQL_CHECK_MSISDN="select * from radchecks where username=?";
    private final String SQL_INS_IP="insert into radreplies (username,attrib,op,value) values (?,'Framed-IP-Address',':=',?)";
    private final String SQL_IN_ROUTE="insert into radreplies (username,attrib,op,value) values (?,'Framed-Route','+=',?)";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Enumeration<String> en = req.getParameterNames();
        Connection connection;
        PrintWriter pw = resp.getWriter();
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
            connection=initConnection();
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
                printResponse(pw,600,null);
                closeConnection();
                return;
            }
            if (exist && getRoute().length() > 0 && getIp().length() > 0) {
                try {
                    PreparedStatement stmt = connection.prepareStatement(SQL_CHECK_MSISDN);
                    stmt.setString(1, getMsisdn());
                    ResultSet resultSet = stmt.executeQuery();
                    if (resultSet.next()) {
                        String msisdnE = resultSet.getString(2);
                        printResponse(pw,600,msisdnE);
                    }
                    stmt.close();
                    closeConnection();
                    return;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
