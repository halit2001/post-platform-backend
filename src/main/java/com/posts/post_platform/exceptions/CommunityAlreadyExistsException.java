package com.posts.post_platform.exceptions;

public class CommunityAlreadyExistsException extends RuntimeException {
    public CommunityAlreadyExistsException(String message) {
        super(message);
    }
}
