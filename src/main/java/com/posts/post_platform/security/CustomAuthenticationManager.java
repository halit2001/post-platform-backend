package com.posts.post_platform.security;

import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.UserRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    @Autowired
    private UserRepositories userRepositories;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This method is responsible for authenticating the user during login.
     * It checks the provided username and password, validates them against the stored credentials,
     * and returns an authenticated token if the credentials are valid.
     *
     * @param authentication The authentication request containing the username and password.
     * @return Authentication object that represents the authenticated user.
     * @throws AuthenticationException If authentication fails (e.g., wrong username or password).
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Optional<User> optionalUser = userRepositories.findUserByUsername(authentication.getName());
        if (optionalUser.isPresent()) {
            if (passwordEncoder().matches(authentication.getCredentials().toString(), optionalUser.get().getPassword())) {
                JwtUserDetails userDetails = JwtUserDetails.create(optionalUser.get());
                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            } else {
                throw new BadCredentialsException("Wrong Password");
            }
        }
        throw new BadCredentialsException("Wrong username");
    }
}
