package edu.tamu.catalog.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import edu.tamu.catalog.auth.model.AppUserDetails;
import edu.tamu.catalog.model.User;
import edu.tamu.catalog.model.repo.UserRepo;
import edu.tamu.weaver.auth.service.AbstractWeaverUserDetailsService;

@Service
public class AppUserDetailsService extends AbstractWeaverUserDetailsService<User, UserRepo> {

    @Override
    public UserDetails buildUserDetails(User user) {
        return new AppUserDetails(user);
    }

}
