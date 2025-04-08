package com.posts.post_platform.mapper;

import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.Status;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.UpdatePostRequest;
import com.posts.post_platform.response.PostResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Component
public class PostMapper {

    public Post convertPostDtoToPostModel(PostDto postDto, String status, User creator, Community community) {
        return Post.builder()
                .content(postDto.getContent())
                .title(postDto.getTitle())
                .status(Status.fromString(status))
                .creator(creator)
                .community(community)
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .comments(new ArrayList<>())
                .build();
    }

    public Post createPostModel(Map<String, Object> post, User user, Community community) {
        return Post.builder()
                .content(post.get("content").toString())
                .title(post.get("title").toString())
                .status(Status.ACTIVE)
                .creator(user)
                .community(community)
                .createdAt(LocalDateTime.parse((String) post.get("requested_at")))
                .likeCount(0)
                .comments(new ArrayList<>())
                .build();
    }

    public PostResponse convertPostToPostResponse(Post post) {
        return PostResponse
                .builder()
                .post_id(post.getId())
                .communityName(post.getCommunity().getCommunityName())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .creatorUsername(post.getCreator().getUsername())
                .status(post.getStatus().name().charAt(0) + post.getStatus().name().substring(1).toLowerCase())
                .build();
    }

    public Post updatePost(UpdatePostRequest updatePostRequest, Post post) {
        if (updatePostRequest.getContent() != null) {
            post.setContent(updatePostRequest.getContent());
        }
        if (updatePostRequest.getTitle() != null) {
            post.setTitle(updatePostRequest.getTitle());
        }
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    public Post copyPostForUpdated(UpdatePostRequest updatePostRequest, Post post) {
        return Post
                .builder()
                .title(updatePostRequest.getTitle())
                .content(updatePostRequest.getContent())
                .creator(post.getCreator())
                .community(post.getCommunity())
                .original_post(post)
                .comments(post.getComments())
                .createdAt(post.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .likeCount(post.getLikeCount())
                .status(Status.PENDING)
                .build();
    }

}
