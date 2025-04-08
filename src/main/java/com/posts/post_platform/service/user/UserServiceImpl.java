package com.posts.post_platform.service.user;

import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.UserRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepositories userRepositories;

    private CommunityRepository communityRepository;

    public Optional<User> getUserByEmail(String email) {
        return userRepositories.findUserByEmail(email);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepositories.findUserByUsername(username);
    }

    /**
     * Retrieves the username from the current authenticated user.
     * This method checks if the authentication object is valid, if the user is authenticated,
     * and if the principal is of type UserDetails. If all conditions are met, it returns the username;
     * otherwise, it returns null.
     */
    @Override
    public String getUsernameFromAuthentication(UserDetails userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) ? userDetails.getUsername() : null;
    }

    /**
     * Retrieves the user ID based on the provided username.
     * This method fetches the user from the database using the username and returns the user ID.
     * If the user is not found, it throws a UserNotFoundException.
     */
    @Override
    public Long getUserId(String username) {
        User user = userRepositories.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return user.getId();
    }

}
