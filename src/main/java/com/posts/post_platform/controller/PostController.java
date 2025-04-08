package com.posts.post_platform.controller;

import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.UpdatePostRequest;
import com.posts.post_platform.response.PendingPost;
import com.posts.post_platform.response.PostResponse;
import com.posts.post_platform.service.community.CommunityService;
import com.posts.post_platform.service.moderation.ModerationService;
import com.posts.post_platform.service.post.PostService;
import com.posts.post_platform.service.user.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private EntityManager entityManager;

    /**
     * This method retrieves a specific post by its ID.
     * The post ID is passed as a path variable in the URL.
     * The service layer is called to fetch the post from the database.
     * If successful, it returns the post details in the response with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @GetMapping("get_post/{post_id}")
    public ResponseEntity<?> getPostById(@PathVariable Long post_id) {
        try {
            PostResponse postResponse = postService.getPostById(post_id);
            return ResponseEntity.status(HttpStatus.OK).body(postResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method retrieves all posts associated with a specific community, identified by the community name.
     * It also takes the authenticated user's details to fetch posts visible to them in that community.
     * If successful, it returns a list of posts in the response with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @GetMapping("get_posts/community_name/{community_name}")
    public ResponseEntity<?> getPostsByCommunity(@PathVariable String community_name, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            List<PostResponse> postResponseList = postService.getPostsByCommunity(community_name, username);
            return ResponseEntity.status(HttpStatus.OK).body(postResponseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method retrieves all posts written by a specific user, identified by their username.
     * The service layer fetches the posts based on the username, and the current authenticated user's username.
     * If successful, it returns a list of posts written by the user in the response with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @GetMapping("get_posts/user/{username}")
    public ResponseEntity<?> getPostsByUsername(@PathVariable String username, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String u_name = (authentication != null && authentication.isAuthenticated()) ? userDetails.getUsername() : null;
            List<PostResponse> postResponseList = postService.getPostsByUsername(username, u_name);
            return ResponseEntity.status(HttpStatus.OK).body(postResponseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method retrieves posts from a specific community, identified by its community ID.
     * The authenticated user's username is passed to ensure proper access control.
     * If successful, it returns a list of posts from the specified community with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @GetMapping("get_posts/community_id/{community_id}")
    public ResponseEntity<?> getPostsByCommunityId(@PathVariable Long community_id, @AuthenticationPrincipal UserDetails userDetails){
        try {
            String username = userDetails.getUsername();
            List<PostResponse> postResponseList = postService.getPostsByCommunityId(community_id, username);
            return ResponseEntity.status(HttpStatus.OK).body(postResponseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method allows the creation of a new post within a community.
     * The community ID is passed as a path variable, and the post data is passed as a request body.
     * It first verifies that the user is authenticated and authorized to post in the community.
     * If the community is public, the post is created directly. If the community is private, the post is either created by moderators or pending approval.
     * The response returns the created post details with HTTP status 201 (Created).
     * If the user is unauthorized or any other error occurs, it returns an appropriate error message.
     */
    @PostMapping("community/{communityId}/create_post")
    @Transactional
    public ResponseEntity<?> createPost(@PathVariable(name = "communityId") Long communityId, @Valid @RequestBody PostDto postDto, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Community community = communityService.findCommunityById(communityId).orElseThrow(() -> new CommunityNotFoundException("Community not found with id : " + communityId));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null && authentication.isAuthenticated()) ? userDetails.getUsername() : null;
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            User user = userService.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
            Community managedCommunity = entityManager.merge(community);
            User managedUser = entityManager.merge(user);
            if (!community.isPrivate()) {
                PostResponse postResponse = postService.createPost(managedCommunity, postDto, managedUser);
                return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
            } else {
                boolean isModerator = managedCommunity.getModerators().stream().anyMatch(mod -> mod.equals(managedUser));
                boolean isMember = managedCommunity.getApprovedUsers().stream().anyMatch(member -> member.equals(managedUser));
                if (managedCommunity.getCreator().equals(managedUser) || isModerator) {
                    PostResponse postResponse = postService.createPost(managedCommunity, postDto, managedUser);
                    return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
                } else if (isMember) {
                    PendingPost pendingPostResponse = moderationService.createPendingPost(managedCommunity, postDto, managedUser);
                    return ResponseEntity.status(HttpStatus.CREATED).body(pendingPostResponse);
                } else {
                    throw new UnauthorizedActionException("You do not have permission to post in this community.");
                }
            }
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (CommunityNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method updates an existing post based on its post ID.
     * It retrieves the current post and updates it with the new details provided in the request body.
     * The service layer is called to perform the update, and the user's username is used for authorization checks.
     * If successful, it returns the updated post in the response with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @PutMapping("update_post/post/{post_id}")
    public ResponseEntity<?> updatePost(@PathVariable Long post_id, @RequestBody UpdatePostRequest updatePostRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null && authentication.isAuthenticated()) ? userDetails.getUsername() : null;
            PostResponse postResponse = postService.updatePost(post_id, updatePostRequest, username);
            return ResponseEntity.status(HttpStatus.OK).body(postResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method deletes a post based on its post ID.
     * The service layer is called to handle the deletion, and the authenticated user's username is used for authorization.
     * If successful, it returns a confirmation message with HTTP status 200 (OK).
     * If any exception occurs, it returns a bad request status (400) with the exception message.
     */
    @DeleteMapping("delete_post/post/{post_id}")
    public ResponseEntity<?> deletePost(@PathVariable Long post_id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null && authentication.isAuthenticated()) ? userDetails.getUsername() : null;
            PostResponse postResponse = postService.deletePost(post_id, username);
            return ResponseEntity.status(HttpStatus.OK).body(postResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
