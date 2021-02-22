package edu.tamu.catalog.auth.model;

import edu.tamu.catalog.model.User;

public class AppUserDetails extends User {

    private static final long serialVersionUID = -7785681787331883261L;

    public AppUserDetails(User user) {
        super(user);
    }

}
