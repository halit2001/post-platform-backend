package com.posts.post_platform.service.comment;

import com.posts.post_platform.requests.CommentRequest;
import com.posts.post_platform.response.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addCommentToPost(String communityName, Long postId, CommentRequest commentRequest, String username);

    CommentResponse replyToComment(String communityName, Long postId, Long commentId, String username, CommentRequest commentRequest);

    CommentResponse getComment(Long postId, String communityName, Long commentId, String username);

    List<CommentResponse> getAllCommentsFromPost(Long postId, String communityName, String username);

    List<CommentResponse> getAllCommentsBySorted(Long postId, String communityName, String username, String sort);

    String likeComment(Long postId, Long commentId, String username);

    String unlikeComment(Long postId, Long commentId, String username);
}
