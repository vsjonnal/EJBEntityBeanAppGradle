package com.test.apps;

import java.io.Serializable;

/**
 *
 * @author vsjonnal
 */
public class TraderPK implements Serializable {

    public String id;

    public TraderPK() {
    }

    public TraderPK(String pk) {
        id = pk;
        if (id == null) {
            throw new AssertionError("Id must not be null.");
        }
    }

    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TraderPK) {
            TraderPK p = (TraderPK) o;
            return p.id.equals(id);
        } else {
            return false;
        }
    }
}
