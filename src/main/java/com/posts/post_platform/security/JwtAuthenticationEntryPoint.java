package com.posts.post_platform.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * This method is invoked when an authentication exception is thrown.
     * It sends an HTTP 401 Unauthorized error with the exception message
     * to indicate that the request has failed due to authentication issues.
     *
     * @param request The HTTP request that triggered the authentication exception.
     * @param response The HTTP response to be sent back to the client.
     * @param authException The authentication exception that occurred (e.g., invalid credentials).
     * @throws IOException If an I/O error occurs while sending the response.
     * @throws ServletException If a servlet-related error occurs while processing the request.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
