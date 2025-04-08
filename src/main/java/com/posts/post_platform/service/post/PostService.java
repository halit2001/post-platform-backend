package com.posts.post_platform.service.post;

import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.UpdatePostRequest;
import com.posts.post_platform.response.PostResponse;

import java.util.List;

public interface PostService {
    PostResponse createPost(Community community, PostDto postDto, User user);

    PostResponse getPostById(Long postId);

    List<PostResponse> getPostsByCommunity(String communityName, String username);

    List<PostResponse> getPostsByCommunityId(Long communityId, String username);

    List<PostResponse> getPostsByUsername(String username, String u_name);

    PostResponse updatePost(Long postId, UpdatePostRequest updatePostRequest, String username);

    PostResponse deletePost(Long postId, String username);
}
