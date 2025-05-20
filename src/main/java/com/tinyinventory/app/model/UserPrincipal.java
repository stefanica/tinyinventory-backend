package com.tinyinventory.app.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private User user;
    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //We don't have Authority Column in the users table (User class)
        //for learning purpose we are going to return a hardcoded value
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }
    // ... there are more methods below that can be implemented

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        //return UserDetails.super.isAccountNonExpired();
        return true; //just for learning
    }

    @Override
    public boolean isAccountNonLocked() {
        //return UserDetails.super.isAccountNonLocked();
        return true; //just for learning
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //return UserDetails.super.isCredentialsNonExpired();
        return true; // just for learning
    }

    @Override
    public boolean isEnabled() {
        //return UserDetails.super.isEnabled();
        return true; //just for learning
    }
}
