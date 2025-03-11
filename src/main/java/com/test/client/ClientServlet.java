package com.test.client;

import com.test.apps.SessionBeanRemote;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author vsjonnal
 */
@WebServlet(name = "ClientServlet", urlPatterns = {"/ClientServlet"})
public class ClientServlet extends HttpServlet {

    @EJB
    private SessionBeanRemote remote;

    private static String pk;

    String jndiName = "java:global.EJBEntityBeanApp.SessionBeanRemote!com.test.apps.SessionBeanRemote";
    String userName = "system";
    String password = "gumby1234";
    //String serverURL = "t3://100.111.143.183:9001";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws javax.naming.NamingException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, NamingException, UnknownHostException {
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();        
        System.out.println("<p>JNDI Context look up for '" + jndiName + "' using user '" + userName + "' and password'"
                + password + "</p>");
        out.println("<br>TEST Started");     
        Context ctx = getContext(userName, password);
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ClientServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h3>Client Servlet context path at " + request.getContextPath() + "</h3>");
            out.println("</body>");
            out.println("</html>");
            
            if (pk == null) {
                pk = String.valueOf(Math.abs(new Random(new java.util.Date().getTime()).nextLong()));
                out.println("<p>PK Created...\n" + pk + "</p>");
            }
            Object obj = ctx.lookup(jndiName);
            out.println("<p>Looked up the object -- SessionBeanRemote" + obj.getClass() + "</p>");
            remote = (SessionBeanRemote) obj;
            out.println("<p>@@@@@ Creating Entity Bean with pk : " + pk + "</p>");
            remote.createEntity(pk);
            out.println("<p>@@@@@ Entity Bean created with pk : " + pk + "</p>");
            testUnlockAfterTxTimeout();
            out.println("<p>@@@@@ Entity Bean testing completed</p>");
            remote.removeEntity(pk);
            out.println("<p>Entity Bean Removed with pk : " + pk + "</p>");
            out.println("<br>TEST Completed");
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            Logger.getLogger(ClientServlet.class.getName()).log(Level.SEVERE, "Exception caugth", e);
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (NamingException ex) {
            Logger.getLogger(ClientServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (NamingException ex) {
            Logger.getLogger(ClientServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void testUnlockAfterTxTimeout() {
        if (callSessionBean()) // 1st call
        {
            if (callSessionBean()) // 2nd call
            {
                return;
            }
        }
        Logger.getLogger("Unexpected to come here");
    }

    private boolean callSessionBean() {
        try {
            String result = remote.test(pk);
            System.out.println("@@@@@ Session's test() result = " + result);
            return false;
        } catch (Throwable th) {
            Throwable rootCause = th;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            if (rootCause instanceof weblogic.transaction.internal.TimedOutException) {
                Logger.getLogger(ClientServlet.class
                        .getName()).log(Level.SEVERE, "@@@@@ Leaving callSessionBean() with a test expected rootCause : \n", getStackTrace(rootCause));
                return true;

            } else {
                Logger.getLogger(ClientServlet.class
                        .getName()).log(Level.SEVERE, "@@@@@ Leaving callSessionBean() with a test unexpected rootCause - ", getStackTrace(rootCause));
                return false;
            }
        }
    }

    private Object getStackTrace(Throwable rootCause) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        rootCause.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private Context getContext(String userName, String password) throws NamingException, UnknownHostException {
        Hashtable<String, String> h = new Hashtable<>();
        InetAddress IP=InetAddress.getLocalHost();
        System.out.println("IP of my system is := "+IP.getHostAddress());
        String serverURL = "t3://"+IP.getHostAddress()+":9001";
        System.out.println("ServerURL is from Client Entity Bean App := "+serverURL);

        h.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        h.put(Context.PROVIDER_URL, serverURL);
        h.put(Context.SECURITY_PRINCIPAL, userName);
        h.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(h);
    }
}
