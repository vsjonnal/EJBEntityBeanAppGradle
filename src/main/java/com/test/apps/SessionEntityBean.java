package com.test.apps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author vsjonnal
 */
public class SessionEntityBean extends CMTraderBean implements EntityBean {

    @Override
    public TraderPK ejbCreate(String id) throws CreateException {
        return create(id, 0);
    }

    @Override
    public TraderPK ejbCreate(String id, int bal)
            throws CreateException {
        return create(id, bal);
    }

    private TraderPK create(String id, int bal)
            throws CreateException {
        System.out.println("##### Creating a bean with key: " + id);

        //Debug.assertion(id != null);
        balance = bal;
        this.id = id;
        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();

        try {
            con = getConnection();
            ps = con.prepareStatement("insert into " + tableName
                    + " (id,balance) values (?,?)");
            ps.setString(1, id);
            ps.setInt(2, balance);
            if (ps.executeUpdate() != 1) {
                throw new CreateException("JDBC did not create any row");
            }

            return new TraderPK(id);
        } catch (SQLException sqe) {
            // Do a findByPK on the id, if it returns something ie. no
            // ObjectNotFoundException thrown, throw DuplicateKeyException
            // otherwise throw an EJBException.
            boolean exists = false;
            try {
                exists = exists(id, con);
            } catch (Exception e) {
                String error = "SQLException: " + sqe;
                throw new CreateException(error);
            }
            if (exists) {
                String error = "SQLException: " + sqe;
                throw new DuplicateKeyException(error);
            } else {
                String error = "SQLException: " + sqe;
                throw new CreateException(error);
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                    //Debug.say("################ [ejbCreate] : Connection closed!");
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    @Override
    public void ejbRemove() throws RemoveException {

        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();

        try {
            id = ((TraderPK) ctx.getPrimaryKey()).getID();
            con = getConnection();
            ps = con.prepareStatement("delete from " + tableName
                    + " where id = ?");
            ps.setString(1, id);
            int ret = ps.executeUpdate();

            System.out.println("##### Removing bean with id: " + ctx.getPrimaryKey());

            if (ret < 1) {
                throw new RemoveException("Error removing bean, ret:" + ret);
            }
        } catch (SQLException sqe) {
            throw new EJBException(sqe.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    public TraderPK ejbFindByPrimaryKey(TraderPK pk)
            throws ObjectNotFoundException {

        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();
        String pkID = pk.getID();
        try {
            con = getConnection();
            System.out.println("################ [ejbFindByPrimaryKey] : Get connection! " + con);
            ps = con.prepareStatement("select id from " + tableName
                    + " where id = ?");
            System.out.println("################ [ejbFindByPrimaryKey] Find bean with id: " + pkID);
            ps.setString(1, pkID);
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                System.out.println("################ [ejbFindByPrimaryKey] : return " + pkID);
                return new TraderPK(pkID);
            } else {
                throw new ObjectNotFoundException(
                        "ejbFindByPrimaryKey: TraderBean (" + id + ") not found");
            }
        } catch (SQLException sqe) {
            throw new EJBException("Error during PK lookup: " + sqe);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                    System.out.println("################ [ejbFindByPrimaryKey] : Connection closed!");
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    public TraderPK ejbFindAccount(String id, int bal)
            throws ObjectNotFoundException {

        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();
        try {
            con = getConnection();
            ps = con.prepareStatement("select id from " + tableName
                    + " where id = ? and balance = ?");
            ps.setString(1, id);
            ps.setInt(2, bal);
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                return new TraderPK(id);
            } else {
                throw new ObjectNotFoundException(
                        "ejbFindAccount: TraderBean (" + id + ") with balance of " + bal
                        + " not found");
            }
        } catch (SQLException sqe) {
            throw new EJBException("Error during PK lookup: " + sqe);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    public Enumeration ejbFindAccountsGreaterThanOrEqualTo(int bal)
            throws FinderException {

        Connection con = null;
        PreparedStatement ps = null;
        Vector v = new Vector();
        String tableName = getTableName();
        try {
            con = getConnection();
            ps = con.prepareStatement("select id from " + tableName
                    + " where balance >= ?");
            ps.setInt(1, bal);
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            while (rs.next()) {
                String id = rs.getString(1);
                v.addElement(new TraderPK(id));
            }

//        if(v.size() == 0) {
//          throw new FinderException (
//            "ejbFindAccountsGreaterThanOrEqualTo: No TraderBeans" +
//             " with balance of "+bal+" not found");
//        }
            return v.elements();

        } catch (SQLException sqe) {
            throw new EJBException("Error during lookup: " + sqe);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    public void ejbLoad() {
        //    Debug.assertion(id != null);
        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();
        id = ((TraderPK) ctx.getPrimaryKey()).getID();
        //Debug.assertion(id != null);
        try {
            con = getConnection();
            ps = con.prepareStatement("select balance from " + tableName
                    + " where id = ?");
            ps.setString(1, id);
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                balance = rs.getInt(1);
            } else {
                if (id == null) {
                    throw new EJBException("id is null!!!!!");
                }
                throw new NoSuchEntityException("Load: TraderBean ("
                        + id + ") not found");
            }
        } catch (SQLException sqe) {
            throw new EJBException("Error loading bean: " + sqe);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    @Override
    public void ejbStore() {
        Connection con = null;
        PreparedStatement ps = null;
        String tableName = getTableName();

        try {
            con = getConnection();
            ps = con.prepareStatement("update " + tableName
                    + " set balance = ? where id = ?");
            ps.setDouble(1, balance);
            ps.setString(2, id);
            int i = ps.executeUpdate();
            if (i == 0) {
                throw new EJBException("ejbStore: TraderBean (" + id
                        + ") not updated");
            }
        } catch (SQLException sqe) {
            throw new EJBException("SQL Error during update: " + sqe);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connections: " + e);
            }
        }
    }

    protected Connection getConnection()
            throws SQLException {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            DataSource ds = (javax.sql.DataSource) initCtx.lookup("java:comp/env/jdbc/testPool");
            Connection con = ds.getConnection();
            return con;
        } catch (NamingException ne) {
            throw new EJBException("Error looking up connection: " + ne);
        } finally {
            try {
                if (initCtx != null) {
                    initCtx.close();
                }
            } catch (NamingException ne) {
                throw new EJBException("Error closing context: " + ne);
            }
        }

    }

    protected String getTableName() {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            return (String) initCtx.lookup("java:comp/env/tableName");
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new EJBException("Error looking up tableName: " + ne);
        } finally {
            try {
                if (initCtx != null) {
                    initCtx.close();
                }
            } catch (NamingException ne) {
                throw new EJBException("Error closing context: " + ne);
            }
        }
    }

    private boolean exists(Object key, Connection con) throws Exception {

        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            java.lang.String pk = (java.lang.String) key;

            java.lang.String query
                    = "select id from " + getTableName() + " where id = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, pk);
            ps.executeQuery();
            rs = ps.getResultSet();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (RuntimeException e) {
            System.out.println("@@@@@ rethrowing RuntimeException."+e);
            throw e;
        } catch (Exception ex) {
            System.out.println("@@@@@ wrapping Exception in EJBException."+ex);
            throw new EJBException(ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                throw new EJBException("Exception while closing connection: " + e);
            }
        }
    }

    public int getBalance() {
        try {
            Thread.sleep(60000);
        } catch (InterruptedException iex) {
            System.out.println("@@@@@ Sleep at getbalance() was interrupted."+ iex);
        }
        return balance;
    }
}
