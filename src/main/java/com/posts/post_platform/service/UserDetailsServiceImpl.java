package com.posts.post_platform.service;

import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.UserRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.posts.post_platform.security.JwtUserDetails;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepositories userRepositories;

    @Autowired
    private UserDetailsServiceImpl(UserRepositories userRepositories) {
        this.userRepositories = userRepositories;
    }

    /**
     * This method loads a user by their username from the database. If the user is found,
     * it returns the user details encapsulated in a JwtUserDetails object. If the user is
     * not found, it throws a UsernameNotFoundException.
     *
     * @param username The username of the user to be loaded.
     * @return The user details encapsulated in a JwtUserDetails object.
     * @throws UsernameNotFoundException If no user is found with the provided username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepositories.findUserByUsername(username);
        if(user.isPresent()) return JwtUserDetails.create(user.get());
        throw new UsernameNotFoundException("User not found with username : " + username);
    }
}
