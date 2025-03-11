package com.test.apps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

/**
 *
 * @author vsjonnal
 */
public class CMTraderBean implements EntityBean {

  public int balance;
  public String id;
  public EntityContext ctx;
 
  public void ejbActivate() {}
  public void ejbRemove() throws RemoveException {}
  public void ejbPassivate() {}
  public void ejbPostCreate(String id) throws CreateException {}
  public void ejbPostCreate(String id, int bal) throws CreateException {}
  public void ejbLoad() {}
  public void ejbStore() {}
 
  public void setEntityContext(EntityContext ctx) {
    this.ctx = ctx;
  }
 
  public void unsetEntityContext() {
    this.ctx = null;
  }
 
  public TraderPK ejbCreate (String id) throws CreateException {
    balance = 0;
    this.id = id;
 
    return null;
  }
 
  public TraderPK ejbCreate (String id, int bal) throws CreateException {
    balance = bal;
    this.id = id;
 
    return null;
  }
 
 
  public int getBalance() {
    return balance;
  }
 
  public void setBalance(int bal) {
    balance = bal;
  }
 
  public void incrementBalance() {
    balance++;
  }
 
  public String getID() {
    return id;
  }
 
  public boolean isContextValid() {
    TraderPK pk = (TraderPK)ctx.getPrimaryKey();
    if(!pk.getID().equals(id)) {
      return false;
    }
    return true;
  }
 
  public boolean isModified() { return true; }
 
  public int getTXIsolationLevel() throws SQLException {
    Connection con = DriverManager.getConnection("jdbc:weblogic:jts:testpool");
    return con.getTransactionIsolation();
  }
}