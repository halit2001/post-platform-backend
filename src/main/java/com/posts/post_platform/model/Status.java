package com.posts.post_platform.model;

public enum Status {
    ACTIVE,
    INACTIVE,
    DELETED,
    PENDING;

    public static Status fromString(String status) {
        if(status != null) {
            for(Status s : Status.values()) {
                if(status.equalsIgnoreCase(s.name())) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("No constant with status " + status + " found");
    }
}
