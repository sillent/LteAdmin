package ru.megafon.lte;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Dmitry Ulyanov on 30.03.15.
 * Senjor Manager
 */
public class Add extends HttpServlet {
    Connector connector;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean existInDb;
        PrintWriter pw = resp.getWriter();
        // Connection initization
        connector = new Connector();


        try {
            existInDb = connector.checkParamExist(req.getParameter("msisdn"));      //проверяем присутствие номера в БД
            if (req.getParameter("msisdn") == null) {
                printResponse(pw, 600, null);
                return;
            }
            // 1 - msisdn+ip
            // 2 - msisdn+ip+route
            // 3 - msisdn+route
            // 0 - other
            int type = doCheck(req);


            if (type == 0) {
                printResponse(pw, 600, null);
                return;
            }

            if (type == 1) {
                String msisdn = req.getParameter("msisdn");
                String ip = ipVerify(req.getParameter("ip"));
                if (ip == null) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (existInDb) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (!existInDb) {
                    connector.insertMsisdnInChecks(msisdn);
                    connector.insertIpInReplies(msisdn, ip);
                    printResponse(pw, 200, null);
                    return;
                }
            }
            if (type == 2) {
                String msisdn = req.getParameter("msisdn");
                String ip = ipVerify(req.getParameter("ip"));
                String route = routeVerify(req.getParameter("route"));


                if (ip == null) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (route == null) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (existInDb) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (!existInDb) {
                    connector.insertMsisdnInChecks(msisdn);
                    connector.insertIpInReplies(msisdn, ip);
                    connector.insertRouteInReplies(msisdn, route);
                    printResponse(pw, 200, null);
                    return;
                }
            }
            if (type == 3) {
                String msisdn = req.getParameter("msisdn");
                String route = routeVerify(req.getParameter("route"));
                if (!existInDb) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (route == null) {
                    printResponse(pw, 600, null);
                    return;
                }
                if (existInDb) {
                    connector.insertRouteInReplies(msisdn, route);
                    printResponse(pw, 200, null);
                    return;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int doCheck(HttpServletRequest req) {
        boolean msisdn = false, ip = false, route = false;
        Enumeration<String> paramNameEnum = req.getParameterNames();
        while (paramNameEnum.hasMoreElements()) {
            String pname = paramNameEnum.nextElement();
            if (pname.equals("msisdn")) {
                msisdn = true;
                System.out.println("msisdn set to true");
            }
            if (pname.equals("ip")) {
                ip = true;
                System.out.println("ip set to true");
            }
            if (pname.equals("route")) {
                route = true;
                System.out.println("route set to true");
            }
            System.out.println("pname = "+pname);
        }
        if (msisdn && !ip && !route) {
            System.out.println("return 0");
            return 0;
        } else if (msisdn && ip && !route) {
            System.out.println("return 1");
            return 1;
        } else if (msisdn && ip && route) {
            System.out.println("return 2");
            return 2;
        } else if (msisdn && !ip && route) {
            System.out.println("return 3");
            return 3;
        } else {
            System.out.println("return else");
            return 0;
        }
    }

    private String ipVerify(String msisdn) {
        String[] arOfOctet = msisdn.split("\\.");

        if (arOfOctet.length == 4) {
            StringBuilder correctIp = new StringBuilder();
            for (int i = 0; i < arOfOctet.length; i++) {
                correctIp.append(Integer.parseInt(arOfOctet[i]));
                if (i!=3)
                    correctIp.append('.');
            }
            return correctIp.toString();
        }
        else
            return null;

    }
    private String routeVerify(String route) {
        if (route.length()>0) {
            String[] arOfNetAndMask = route.split("\\/");
            if (arOfNetAndMask.length==2) {
                String arOfOctet = ipVerify(arOfNetAndMask[0]);         // октеты сети
                Integer mask = Integer.parseInt(arOfNetAndMask[1]);     // маска сети
                if (arOfOctet == null) {
                    return null;
                }
                if (mask == null) {
                    return null;
                }
                return arOfOctet + "/" + mask;
            }
            else
                return null;
        }
        else
            return null;

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
