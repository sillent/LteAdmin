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
        PrintWriter pw=resp.getWriter();
        try {
            existInDb = connector.checkParamExist(req.getParameter("msisdn"));

            if (!existInDb) {
                printResponse(pw, 200, null);
                System.out.println("not exist in delete");
                return;
            }
            if (existInDb) {
                connector.deleteMsisdn(req.getParameter("msisdn"));
                printResponse(pw, 200, null);
                System.out.println("exist in delete");
                return;
            }

        } catch (SQLException e) {
            printResponse(pw,600,null);
            return;
        }
    }

    private void printResponse(PrintWriter pw, int code, String msisdn_exist) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<result>");
        pw.println("<code>" + code + "</code>");
        if (msisdn_exist != null) {
            pw.println("<msisdn>" + msisdn_exist + "</msisdn>");
        }
        pw.println("</result>");
    }
}
