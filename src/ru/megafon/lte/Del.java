package ru.megafon.lte;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Created by santa on 30.03.15.
 */
public class Del extends HttpServlet {
    Connector connector;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean existInDb;
        connector = new Connector();
        try {
            existInDb = connector.checkParamExist(req.getParameter("msisdn"));

            if (!existInDb) {
                printResponse(resp, 200, null);
                connector.closeConnection();
                return;
            }
            if (existInDb) {
                connector.deleteMsisdn(req.getParameter("msisdn"));
                printResponse(resp, 200, null);
                connector.closeConnection();
                return;
            }

        } catch (SQLException e) {
            printResponse(resp, 600, null);
            connector.closeConnection();
            return;
        }
    }

    private void printResponse(HttpServletResponse response, int code, String msisdn_exist) throws IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<result>");
        pw.println("<code>" + code + "</code>");
        if (msisdn_exist != null) {
            pw.println("<msisdn>" + msisdn_exist + "</msisdn>");
        }
        pw.println("</result>");
    }
}
