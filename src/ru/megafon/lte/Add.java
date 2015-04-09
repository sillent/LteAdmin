package ru.megafon.lte;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
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
        // Connection initization
        connector = new Connector();


        try {
            existInDb = connector.checkParamExist(req.getParameter("msisdn"));      //проверяем присутствие номера в БД
            if (req.getParameter("msisdn") == null) {
                printResponse(resp, 600, null);
                connector.closeConnection();
                return;
            }
            // 1 - msisdn+ip
            // 2 - msisdn+ip+route
            // 3 - msisdn+route
            // 0 - other
            int type = doCheck(req);


            if (type == 0) {
                printResponse(resp, 600, null);
                connector.closeConnection();
                return;
            }

            if (type == 1) {
                String msisdn = req.getParameter("msisdn");
                String ip = ipVerify(req.getParameter("ip"));
                if (ip == null) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (existInDb) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (!existInDb) {
                    connector.insertMsisdnInChecks(msisdn);
                    connector.insertIpInReplies(msisdn, ip);
                    printResponse(resp, 200, null);
                    connector.closeConnection();
                    return;
                }
            }
            if (type == 2) {
                String msisdn = req.getParameter("msisdn");
                String ip = ipVerify(req.getParameter("ip"));
                String route = routeVerify(req.getParameter("route"));


                if (ip == null) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (route == null) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (existInDb) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (!existInDb) {
                    connector.insertMsisdnInChecks(msisdn);
                    connector.insertIpInReplies(msisdn, ip);
                    connector.insertRouteInReplies(msisdn, route);
                    printResponse(resp, 200, null);
                    connector.closeConnection();
                    return;
                }
            }
            if (type == 3) {
                String msisdn = req.getParameter("msisdn");
                String route = routeVerify(req.getParameter("route"));
                if (!existInDb) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (route == null) {
                    printResponse(resp, 600, null);
                    connector.closeConnection();
                    return;
                }
                if (existInDb) {
                    connector.insertRouteInReplies(msisdn, route);
                    printResponse(resp, 200, null);
                    connector.closeConnection();
                    return;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            connector.closeConnection();
        }
    }

    public int doCheck(HttpServletRequest req) {
        boolean msisdn = false, ip = false, route = false;
        Enumeration<String> paramNameEnum = req.getParameterNames();
        while (paramNameEnum.hasMoreElements()) {
            String pname = paramNameEnum.nextElement();
            if (pname.equals("msisdn")) {
                msisdn = true;
            }
            if (pname.equals("ip")) {
                ip = true;
            }
            if (pname.equals("route")) {
                route = true;
            }
        }
        if (msisdn && !ip && !route) {
            return 0;
        } else if (msisdn && ip && !route) {
            return 1;
        } else if (msisdn && ip && route) {
            return 2;
        } else if (msisdn && !ip && route) {
            return 3;
        } else {
            return 0;
        }
    }

    private String ipVerify(String msisdn) {
        String[] arOfOctet = msisdn.split("\\.");

        if (arOfOctet.length == 4) {
            StringBuilder correctIp = new StringBuilder();
            for (int i = 0; i < arOfOctet.length; i++) {
                if (arOfOctet[i].length() > 0) {
                    try {
                        correctIp.append(Integer.parseInt(arOfOctet[i]));
                    } catch (NumberFormatException nme) {
                        return null;
                    }
                    if (i != 3)
                        correctIp.append('.');
                } else
                    return null;
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
                try {
                    Integer mask = Integer.parseInt(arOfNetAndMask[1]);     // маска сети

                    if (arOfOctet == null) {
                        return null;
                    }
                    if (mask == null) {
                        return null;
                    }
                    return arOfOctet + "/" + mask;
                } catch (NumberFormatException nmbe) {
                    return null;
                }
            }
            else
                return null;
        }
        else
            return null;

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
