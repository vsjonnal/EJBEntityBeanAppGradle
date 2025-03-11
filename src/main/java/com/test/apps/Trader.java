package com.test.apps;

import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.ejb.EJBObject;

/**
 *
 * @author vsjonnal
 */
public interface Trader extends EJBObject {

    public int getBalance() throws RemoteException;

    public void setBalance(int bal) throws RemoteException;

    public void incrementBalance() throws RemoteException;

    public String getID() throws RemoteException;

    public boolean isContextValid() throws RemoteException;

    public int getTXIsolationLevel() throws RemoteException, SQLException;
}