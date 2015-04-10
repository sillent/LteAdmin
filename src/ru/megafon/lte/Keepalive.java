package ru.megafon.lte;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by santa on 10.04.15.
 */
public class Keepalive extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        printResponse(resp,200,null);
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
