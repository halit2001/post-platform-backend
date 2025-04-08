package com.posts.post_platform.service.user;

import com.posts.post_platform.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByUsername(String username);

    String getUsernameFromAuthentication(UserDetails userDetails);

    Long getUserId(String username);
}
