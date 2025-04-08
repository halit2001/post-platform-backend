package com.posts.post_platform.model;

public enum AccessLevel {
    PUBLIC,
    PRIVATE;

    public static AccessLevel fromString(String access_level) {
        if(access_level != null) {
            String controlled_access_level = access_level.trim();
            for(AccessLevel accessLevel : AccessLevel.values()) {
                if(controlled_access_level.equalsIgnoreCase(accessLevel.name())) {
                    return accessLevel;
                }
            }
        }
        throw new IllegalArgumentException("No constant with access level " + access_level + " found");
    }
}
