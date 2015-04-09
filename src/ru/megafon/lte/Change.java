package ru.megafon.lte;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Created by santa on 07.04.15.
 */
public class Change extends HttpServlet {
    Connector connector;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        connector = new Connector();

        int check = doCheckParameter(req);
        if (check==0) {                             // 0 - msisdn & msisdnNew not exist
            printResponse(resp, 300, null);
            connector.closeConnection();
            return;
        }
        if (check==3) {                             // 3 - msisdnNew exist, msisdn not
            printResponse(resp, 300, null);
            connector.closeConnection();
            return;
        }
        if (check == 2) {                           // 2 - msisdn  exist, msisdnNew not
            printResponse(resp, 300, null);
            connector.closeConnection();
            return;
        }
        if (check == 1) {                           // 1 - msisdn & msisdnNew  exist
            try {
                if (connector.checkParamExist(req.getParameter("msisdn"))) {
                    String _old = req.getParameter("msisdn");
                    String _new = req.getParameter("msisdnNew");
                    connector.update(_old, _new);
                    printResponse(resp, 200, null);
                    connector.closeConnection();
                    return;
                } else {
                    printResponse(resp, 300, null);
                    connector.closeConnection();
                    return;
                }
            } catch (SQLException sqlExc) {
                printResponse(resp, 666, null);
                connector.closeConnection();
                return;
            }

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

    private int doCheckParameter(HttpServletRequest request) {
        boolean msisdn,msisdnNew;
        msisdn = false;
        msisdnNew = false;
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String pm = params.nextElement();
            if (pm.equals("msisdn")) {
                msisdn=true;
            }
            if (pm.equals("msisdnNew")) {
                msisdnNew=true;
            }
        }
        if (msisdn && msisdnNew) {                      // 1 - msisdn & msisdnNew  exist
            return 1;                                   // 2 - msisdn  exist, msisdnNew not
        }                                               // 3 - msisdnNew exist, msisdn not
        else if (msisdn && !msisdnNew) {                // 0 - msisdn & msisdnNew not exist
            return 2;
        }
        else if (!msisdn && msisdnNew) {
            return 3;
        }
        else  {
            return 0;
        }
    }

}
