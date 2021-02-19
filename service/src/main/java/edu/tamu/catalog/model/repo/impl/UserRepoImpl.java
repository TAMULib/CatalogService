package edu.tamu.catalog.model.repo.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import edu.tamu.catalog.model.User;
import edu.tamu.catalog.model.repo.UserRepo;
import edu.tamu.catalog.model.repo.UserRepoCustom;
import edu.tamu.weaver.data.model.repo.impl.AbstractWeaverRepoImpl;

/**
 * UserRepoImpl - implementation of user repository
 *
 * @author
 */
public class UserRepoImpl extends AbstractWeaverRepoImpl<User, UserRepo> implements UserRepoCustom {

    @Autowired
    private UserRepo userRepo;

    @Override
    public synchronized User create(User user) {
        Optional<User> returnableUser = userRepo.findByUsername(user.getUsername());
        return returnableUser.isPresent() ? returnableUser.get() : userRepo.save(user);
    }

    @Override
    protected String getChannel() {
        return "/channel/users";
    }

}