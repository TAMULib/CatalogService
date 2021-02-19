/*
 * AppUser.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */
package edu.tamu.catalog.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.tamu.catalog.enums.Role;
import edu.tamu.weaver.auth.model.AbstractWeaverUserDetails;
import edu.tamu.weaver.auth.model.Credentials;
import edu.tamu.weaver.user.model.IRole;

/**
 * Application User entity.
 *
 */
@Entity
public class User extends AbstractWeaverUserDetails {

    private static final long serialVersionUID = -4664788277156836978L;

    @Column(nullable = false)
    private Role role;

    @Column(nullable = true)
    private String netid;

    @Column(nullable = true, unique = true)
    private String email;

    // encoded password
    @JsonIgnore
    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    /**
     * Constructor for the application user
     *
     */
    public User() {
        super();
    }

    public User(User user) {
        this();
        setUsername(user.getUsername());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        // TODO: these probably should be added.
        //setEmail(user.getEmail());
        //setNetid(user.getNetid());
        setRole(user.getRole());
    }

    public User(Credentials credentials) {
        this();
        setUsername(credentials.getUin());
        setFirstName(credentials.getFirstName());
        setLastName(credentials.getLastName());
        // TODO: these probably should be added.
        //setEmail(credentials.getEmail());
        //setNetid(credentials.getNetid());
        setRole(Role.valueOf(credentials.getRole()));
    }

    /**
     * Constructor for application user with uin passed.
     *
     * @param uin
     *            Long
     *
     */
    public User(String email, String firstName, String lastName, Role role) {
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setRole(role);
    }

    /**
     * @return the role
     */
    @JsonDeserialize(as = Role.class)
    public IRole getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    @JsonSerialize(as = Role.class)
    public void setRole(IRole role) {
        this.role = (Role) role;
    }

    /**
     * @return the netid
     */
    public String getNetid() {
        return netid;
    }

    /**
     * @param netid
     *            the netid to set
     */
    public void setNetid(String netid) {
        this.netid = netid;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return firstName
     *
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            String
     *
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return lastName
     *
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            String
     *
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(this.getRole().toString());
        authorities.add(authority);
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return null;
    }
}
