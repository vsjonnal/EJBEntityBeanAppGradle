package com.test.apps;

import java.rmi.RemoteException;
import java.util.Enumeration;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author vsjonnal
 */
public interface TraderHome extends EJBHome {

    Trader create(String id) throws CreateException, RemoteException;

    Trader create(String id, int bal) throws CreateException, RemoteException;

    Trader findAccount(String id, int bal) throws FinderException, RemoteException;

    Trader findByPrimaryKey(TraderPK eoPK) throws FinderException, RemoteException;

    Enumeration findAccountsGreaterThanOrEqualTo(int bal) throws FinderException, RemoteException;
}