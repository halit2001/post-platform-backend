package com.posts.post_platform.mapper;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.model.Role;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserMapper {
    @Autowired
    private UserRepositories userRepositories;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto convertRegisterRequestToUserDto(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.USER);
        user.setRole(roleList);
        userRepositories.save(user);
        return this.convertUserToUserDto(user);
    }

    public UserDto convertUserToUserDto(User user) {
        return UserDto
                .builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
