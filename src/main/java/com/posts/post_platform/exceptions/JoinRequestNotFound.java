package com.posts.post_platform.exceptions;

public class JoinRequestNotFound extends RuntimeException {
    public JoinRequestNotFound(String message) {
        super(message);
    }
}
