package com.posts.post_platform.controller;

import com.posts.post_platform.exceptions.*;
import com.posts.post_platform.requests.CommentRequest;
import com.posts.post_platform.response.CommentResponse;
import com.posts.post_platform.service.comment.CommentService;
import com.posts.post_platform.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    /**
     * This method retrieves all comments for a specific post in a specific community.
     * It returns a list of comments or an error message if something goes wrong.
     *
     * @param postId The ID of the post.
     * @param communityName The name of the community.
     * @param userDetails The authenticated user's details.
     * @return A list of all comments or an error message.
     */
    @GetMapping("/community/{communityName}/post/{postId}/get_all_comments")
    public ResponseEntity<?> getAllCommentsFromPost(@PathVariable(name = "postId") Long postId,
                                                    @PathVariable(name = "communityName") String communityName,
                                                    @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            List<CommentResponse> responseList = commentService.getAllCommentsFromPost(postId, communityName, username);
            return ResponseEntity.status(HttpStatus.OK).body(responseList);
        }  catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method retrieves all comments for a specific post in a specific community, sorted by old.
     * It takes a sorting parameter and returns the comments in the specified order.
     *
     * @param postId The ID of the post.
     * @param communityName The name of the community.
     * @param sort The sorting criteria (e.g., ascending or descending).
     * @param userDetails The authenticated user's details.
     * @return A sorted list of all comments or an error message.
     */
    @GetMapping("/community/{communityName}/post/{postId}/get_all_comments_sorted_by_old")
    public ResponseEntity<?> getAllCommentsFromPostSortedByOld(@PathVariable(name = "postId") Long postId,
                                                               @PathVariable(name = "communityName") String communityName,
                                                               @RequestParam(name = "sort") String sort,
                                                               @AuthenticationPrincipal UserDetails userDetails) throws Exception{
       try {
           String username = userService.getUsernameFromAuthentication(userDetails);
           List<CommentResponse> responseList = commentService.getAllCommentsBySorted(postId, communityName, username, sort);
           return ResponseEntity.status(HttpStatus.OK).body(responseList);
       } catch (UnauthorizedActionException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
       }
    }

    /**
     * This method retrieves all comments sorted by a specific criteria (e.g., top comments) for a specific post.
     * It provides flexibility in sorting comments based on user input.
     *
     * @param communityName The name of the community.
     * @param postId The ID of the post.
     * @param sort The sorting criteria (e.g., top comments).
     * @param userDetails The authenticated user's details.
     * @return A sorted list of all comments or an error message.
     */
    @GetMapping("/get_all_comments_by_sorted_top")
    public ResponseEntity<?> getAllCommentsBySortedTop(@RequestParam("communityName") String communityName,
                                                       @RequestParam("postId") Long postId,
                                                       @RequestParam("sort") String sort,
                                                       @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            List<CommentResponse> responseList = commentService.getAllCommentsBySorted(postId, communityName, username, sort);
            return ResponseEntity.status(HttpStatus.OK).body(responseList);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method retrieves a specific comment from a post in a specific community by its ID.
     * It ensures that the user is authenticated and authorized to view the comment.
     *
     * @param postId The ID of the post.
     * @param communityName The name of the community.
     * @param commentId The ID of the comment.
     * @param userDetails The authenticated user's details.
     * @return The requested comment or an error message.
     */
    @GetMapping("/community/{communityName}/post/{postId}")
    public ResponseEntity<?> getCommentFromPost(@PathVariable(name = "postId") Long postId,
                                                @PathVariable(name = "communityName") String communityName,
                                                @RequestParam Long commentId,
                                                @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            CommentResponse response = commentService.getComment(postId, communityName, commentId, username);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method allows a user to add a comment to a specific post in a specific community.
     * The user must be authenticated to add a comment.
     *
     * @param postId The ID of the post.
     * @param communityName The name of the community.
     * @param commentRequest The comment data.
     * @param userDetails The authenticated user's details.
     * @return The added comment or an error message.
     */
    @PostMapping("/community/{communityName}/post/{postId}")
    public ResponseEntity<?> addCommentToPost(@PathVariable(name = "postId") Long postId,
                                              @PathVariable(name = "communityName") String communityName,
                                              @RequestBody CommentRequest commentRequest,
                                              @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            CommentResponse response = commentService.addCommentToPost(communityName, postId, commentRequest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedActionException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method allows a user to reply to an existing comment on a specific post in a specific community.
     * The user must be authenticated to reply.
     *
     * @param communityName The name of the community.
     * @param postId The ID of the post.
     * @param commentId The ID of the comment being replied to.
     * @param commentRequest The reply data.
     * @param userDetails The authenticated user's details.
     * @return The reply comment or an error message.
     */
    @PostMapping("/community/{communityName}/post/{postId}/reply")
    public ResponseEntity<?> replyToComment(@PathVariable(name = "communityName") String communityName,
                                            @PathVariable(name = "postId") Long postId,
                                            @RequestParam Long commentId,
                                            @RequestBody CommentRequest commentRequest,
                                            @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            CommentResponse commentResponse = commentService.replyToComment(communityName, postId, commentId, username, commentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommunityNotFoundException | CommentNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method allows a user to like a comment on a specific post in a specific community.
     * The user must be authenticated to like a comment.
     *
     * @param postId The ID of the post.
     * @param commentId The ID of the comment.
     * @param userDetails The authenticated user's details.
     * @return The updated like count or an error message.
     */
    @PostMapping("like_comment")
    public ResponseEntity<?> likeComment(@RequestParam("postId") Long postId, @RequestParam("commentId") Long commentId, @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            String likeCount = commentService.likeComment(postId, commentId, username);
            return ResponseEntity.status(HttpStatus.OK).body(likeCount);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method allows a user to unlike a comment on a specific post in a specific community.
     * The user must be authenticated to unlike a comment.
     *
     * @param postId The ID of the post.
     * @param commentId The ID of the comment.
     * @param userDetails The authenticated user's details.
     * @return The updated like count or an error message.
     */
    @PostMapping("unlike_comment")
    public ResponseEntity<?> unlikeComment(@RequestParam("postId") Long postId, @RequestParam("commentId") Long commentId, @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            String unlikeCount = commentService.unlikeComment(postId, commentId, username);
            return ResponseEntity.status(HttpStatus.OK).body(unlikeCount);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
