package com.test.apps;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import weblogic.ejb.container.EJBLogger;
import weblogic.ejb20.locks.LockTimedOutException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author vsjonnal
 */
@Stateless(name = "SessionBeanRemote", mappedName = "SessionBeanRemote")
public class SessionBean implements SessionBeanRemote {
    
    Context ctx;
    String jndiName="TraderHome";

    public SessionBean() throws NamingException, UnknownHostException {
        this.ctx = getContext("system", "gumby1234");
    }

    public void createEntity(String pk) {
        TraderHome home = null;
        Trader trader;
        try {
            //Context ctx = new InitialContext();
            home = getTraderHome(ctx);
            trader = home.create(pk);
            String msg;
            msg = "##### Created Trader = " + trader + "pk = " +pk;
            Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, msg);
        } catch (DuplicateKeyException dkex) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Expected to failed to clean up last time.  Try to remove and create with primary key = " + pk + "\n", dkex);
            try {
                trader = findByPrimaryKey(home, pk);
                trader.remove();
                Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, "##### Removed Trader pk =", pk);
                trader = home.create(pk);
                String msg;
                msg = "##### Created Trader = " + trader + "pk = " +pk;
                Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, msg);
            } catch (Exception ex) {
                Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Could not remove and create Entity Object.\n", ex);
                throw new EJBException(ex);
            }
        } catch (Exception ex) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Could not create Entity Object.\n", ex);
            throw new EJBException(ex);
        }
    }

    public String test(String pk) {
        try {
            //Context ctx = new InitialContext();
            TraderHome home = getTraderHome(ctx);
            Trader trader = findByPrimaryKey(home, pk);
            trader.getBalance();
            return "Completed to call EntityBean";
        } catch (NamingException nex) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Could not get Entity Home.\n", nex);
            return "NamingException : Could not get Entity Home. \n" + EJBLogger.logStackTraceLoggable(nex).getMessage();
        } catch (ObjectNotFoundException onfe) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Could not find Entity (pk = " + pk + " ).\n", onfe);
            return "ObjectNotFoundException : Could not find Entity (pk = " + pk + " ).\n" + EJBLogger.logStackTraceLoggable(onfe).getMessage();
        } catch (Throwable th) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Unexpected exception at calling Entity.\n", th);

            Throwable rootCause = th;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            String fullStackTrace = EJBLogger.logStackTraceLoggable(rootCause).getMessage();
            if (rootCause instanceof LockTimedOutException) {
                return "LockTimeoutException : BUG25195456 fix did not work well.\n" + fullStackTrace;
            } else {
                return "Unexpected Exception : Not related with BUG25195456.\n" + fullStackTrace;
            }
        }
    }

    public void removeEntity(String pk) {
        try {
            //Context ctx = new InitialContext();
            TraderHome home = getTraderHome(ctx);
            Trader trader = findByPrimaryKey(home, pk);
            trader.remove();
            Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, "##### Removed EntityBean with primary key = ", pk);
        } catch (Exception ex) {
            Logger.getLogger(SessionBean.class.getName()).log(Level.SEVERE, "@@@@@ Unexpected exception at calling EntityBean remove.\n", ex);
            throw new EJBException("Unexpected exception at calling EntityBean remove", ex);
        }
    }

    private TraderHome getTraderHome(Context ctx) throws NamingException {
        Object obj = ctx.lookup(jndiName);
        TraderHome home = (TraderHome) PortableRemoteObject.narrow(obj, TraderHome.class);
        Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, "##### Got TraderHome = ",home);
        return home;
    }

    private Trader findByPrimaryKey(TraderHome home, String pk) throws FinderException, RemoteException {
        TraderPK tpk = new TraderPK(pk);
        Trader trader = (Trader) home.findByPrimaryKey(tpk);
        Logger.getLogger(SessionBean.class.getName()).log(Level.INFO, "##### Got Trader = ", trader);
        return trader;
    }
    
    private Context getContext(String userName, String password) throws NamingException, UnknownHostException {
        Hashtable<String, String> h = new Hashtable<>();
        
        InetAddress IP=InetAddress.getLocalHost();
        System.out.println("IP of my system is := "+IP.getHostAddress());
        String serverURL = "t3://"+IP.getHostAddress()+":9001";
        System.out.println("ServerURL is from Entity Bean App := "+serverURL);
        
        h.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        h.put(Context.PROVIDER_URL, serverURL);
        h.put(Context.SECURITY_PRINCIPAL, userName);
        h.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(h);
    }
}
