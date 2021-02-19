/*
 * UserRepoCustom.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */
package edu.tamu.catalog.model.repo;

import edu.tamu.catalog.model.User;

/**
 *
 */
public interface UserRepoCustom {

    /**
     * method to delete application user
     *
     * @param user
     *            AppUser
     */
    public void delete(User user);

}
