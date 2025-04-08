package com.posts.post_platform.dto;

import com.posts.post_platform.model.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private List<Role> role;
}
