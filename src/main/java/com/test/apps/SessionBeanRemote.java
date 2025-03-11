package com.test.apps;

import javax.ejb.Remote;

/**
 *
 * @author vsjonnal
 */
@Remote
public interface SessionBeanRemote {

    void createEntity(String pk);

    String test(String pk);

    void removeEntity(String pk);
}
