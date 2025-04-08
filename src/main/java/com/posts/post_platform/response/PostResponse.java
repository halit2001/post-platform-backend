package com.posts.post_platform.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
public class PostResponse {
    private Long post_id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String status;
    private String creatorUsername;
    private String communityName;
}
