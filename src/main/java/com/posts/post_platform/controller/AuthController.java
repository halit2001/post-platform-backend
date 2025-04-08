package com.posts.post_platform.controller;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.mapper.UserMapper;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.RegisterRequest;
import com.posts.post_platform.requests.UserRequest;
import com.posts.post_platform.security.JwtTokenProvider;
import com.posts.post_platform.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    private final UserMapper userMapper;

    @Autowired
    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * This method handles the login functionality. It accepts the username and password,
     * authenticates the user, and generates a JWT token if the authentication is successful.
     *
     * @param loginRequest The login request containing the username and password.
     * @return A ResponseEntity with the generated JWT token if authentication is successful.
     */
    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody UserRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication auth = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(jwtTokenProvider.generateJwtToken(auth));
    }

    /**
     * This method handles user registration. It first checks if the email provided is already registered.
     * If the email is already registered, it returns a BAD REQUEST response.
     * Otherwise, it converts the registration request into a UserDto and returns it.
     *
     * @param registerRequest The registration request containing user details.
     * @return A ResponseEntity with the UserDto if registration is successful, or an error message if the user is already registered.
     */
    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        Optional<User> user = userService.getUserByEmail(registerRequest.getEmail());
        if(user.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User has already been registered");
        }
        UserDto userDto = userMapper.convertRegisterRequestToUserDto(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

}
