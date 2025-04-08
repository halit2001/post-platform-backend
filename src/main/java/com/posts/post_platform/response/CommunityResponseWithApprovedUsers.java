package com.posts.post_platform.response;

import com.posts.post_platform.dto.UserDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponseWithApprovedUsers {
    private Long creatorId;
    private String community_name;
    private String description;
    private LocalDateTime createdAt;
    private List<String> topics;
    private String access_level;
    private List<UserDto> approvedUsers;
}
