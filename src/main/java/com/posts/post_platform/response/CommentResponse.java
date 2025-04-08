package com.posts.post_platform.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String commentStatus;
    private Long commentId;
    private String content;
    private Long postId;
    private String postName;
    private String commentAuthorName;
    private Long parentCommentId;
    private List<CommentResponse> childComments;
}
