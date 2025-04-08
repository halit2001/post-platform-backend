package com.posts.post_platform.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotNull
    private String username;

    @NotNull
    @Size(min = 4, max = 18, message = "Password must be between 4 and 18 length")
    private String password;
}
