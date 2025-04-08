package com.posts.post_platform.mapper;

import com.posts.post_platform.model.Comment;
import com.posts.post_platform.model.CommentStatus;
import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommentRepository;
import com.posts.post_platform.requests.CommentRequest;
import com.posts.post_platform.response.CommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommentMapper {
    @Autowired
    private CommentRepository commentRepository;

    public Comment addCommentToPost(CommentRequest commentRequest, Post post, User user) {
        return Comment.builder()
                .commentStatus(CommentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .content(commentRequest.getContent())
                .post(post)
                .commentAuthor(user)
                .parentComment(null)
                .build();
    }

    public CommentResponse convertCommentToResponse(Comment comment) {
        Long parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        return CommentResponse.builder()
                .commentStatus(comment.getCommentStatus().name())
                .commentId(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .postName(comment.getPost().getTitle())
                .commentAuthorName(comment.getCommentAuthor().getUsername())
                .parentCommentId(parentCommentId)
                .childComments(null)
                .build();
    }

    public CommentResponse convertCommentToResponseWithChildComments(Comment comment) {
        Long parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        List<CommentResponse> childComments = (comment.getChildComments() != null && !comment.getChildComments().isEmpty()) ?
                comment.getChildComments().stream().map(this::convertCommentToResponseWithChildComments).toList() : null;
        return CommentResponse.builder()
                .commentStatus(comment.getCommentStatus().name())
                .commentId(comment.getId())
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .postName(comment.getPost().getTitle())
                .commentAuthorName(comment.getCommentAuthor().getUsername())
                .parentCommentId(parentCommentId)
                .childComments(childComments)
                .build();
    }

    public List<CommentResponse> convertAllCommentsToResponse(List<Comment> comments) {
        return comments.stream().map(this::convertCommentToResponseWithChildComments).toList();
    }

    public Comment replyToComment(CommentRequest commentRequest, Post post, User user, Comment parentComment) {
        return Comment.builder()
                .commentStatus(CommentStatus.ACTIVE)
                .content(commentRequest.getContent())
                .post(post)
                .commentAuthor(user)
                .parentComment(parentComment)
                .createdAt(LocalDateTime.now())
                .childComments(null)
                .build();
    }

}
