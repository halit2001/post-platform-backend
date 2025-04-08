package com.posts.post_platform.response;

import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityWithAdditionalDataResponse {
    private Long creatorId;
    private String community_name;
    private String description;
    private LocalDateTime createdAt;
    private List<String> topics;
    private String access_level;
    private List<User> approvedUsers;
    private List<User> moderators;
    private List<Post> posts;
}
