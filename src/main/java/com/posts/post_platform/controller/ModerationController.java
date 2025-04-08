package com.posts.post_platform.controller;

import com.posts.post_platform.dto.ApproveJoinRequestDto;
import com.posts.post_platform.dto.RejectJoinRequestDto;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.PostNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.requests.AddModeratorRequest;
import com.posts.post_platform.requests.AddModeratorsToCommunityRequest;
import com.posts.post_platform.response.CommunityWithAdditionalDataResponse;
import com.posts.post_platform.response.PostResponse;
import com.posts.post_platform.service.moderation.ModerationService;
import com.posts.post_platform.service.user.UserService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/moderation")
public class ModerationController {
    @Autowired
    private ModerationService moderationService;

    @Autowired
    private UserService userService;

    /**
     * This method handles the GET request to find the post index for a specific community and post.
     */
    @GetMapping("/community/{communityId}/post/{postId}/get_post_request")
    public ResponseEntity<Integer> findPostIndex(@PathVariable("communityId") Long communityId,
                                                 @PathVariable("postId") String postId) {
        int index = moderationService.findPostIndex(communityId, postId);
        return ResponseEntity.status(HttpStatus.OK).body(index);
    }

    /**
     * This method handles the GET request to find the user index for a specific community and user.
     */
    @GetMapping("/community/{communityId}/user/{userId}/get_user_request")
    public ResponseEntity<Integer> findPostIndex(@PathVariable("communityId") Long communityId, @PathVariable("userId") Long userId) {
        int index = moderationService.findUserIndex(communityId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(index);
    }

    /**
     * This method handles the GET request to retrieve all pending join requests for a specific community.
     */
    @GetMapping("/community/{community_id}/get_pending_join_requests")
    public ResponseEntity<?> getPendingJoinRequests(@PathVariable Long community_id, @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            List<Map<String, Object>> responses = moderationService.getPendingJoinRequests(community_id, username);
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } catch (UserNotFoundException | CommunityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the GET request to pull all pending post requests linked to a specific community from Redis.
     */
    @GetMapping("/community/{communityId}/get_all_pending_posts")
    public ResponseEntity<?> getPendingPostRequests(@PathVariable("communityId") Long communityId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            List<Map<String, Object>> result = moderationService.getPendingPostRequests(communityId, username);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (UserNotFoundException | CommunityNotFoundException e ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to approve a join request for a specific user in a community.
     */
    @PostMapping("/community/{communityId}/join_request/{user_id}/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable(name = "communityId") Long communityId,
                                                @PathVariable(name = "user_id") Long userId,
                                                @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            ApproveJoinRequestDto approved = moderationService.approveJoinRequest(communityId, userId, username);
            return ResponseEntity.status(HttpStatus.OK).body(approved);
        } catch (UserNotFoundException | CommunityNotFoundException | EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to approve a post request for a specific post in a community.
     */
    @PostMapping("/community/{communityId}/post/{postId}/approve_post_request")
    public ResponseEntity<?> approvePostRequest(@PathVariable(name = "communityId") Long communityId,
                                                @PathVariable(name = "postId") String postId,
                                                @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            PostResponse response = moderationService.approvePostRequest(communityId, postId, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserNotFoundException | CommunityNotFoundException | PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to reject a join request for a specific user in a community.
     */
    @PostMapping("/community/{communityId}/join_request/{user_id}/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable(name = "communityId") Long communityId,
                                               @PathVariable(name = "user_id") Long userToAttendId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            RejectJoinRequestDto rejected = moderationService.rejectJoinRequest(communityId, userToAttendId, username);
            return ResponseEntity.status(HttpStatus.OK).body(rejected);
        }  catch (UserNotFoundException | CommunityNotFoundException | EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to reject a post request for a specific post in a community.
     */
    @PostMapping("/community/{communityId}/post/{postId}/reject_post_request")
    public ResponseEntity<?> rejectPostRequest(@PathVariable("communityId") Long communityId, @PathVariable("postId") String postId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            String response = moderationService.rejectPostRequest(communityId, postId, username);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }  catch (UserNotFoundException | CommunityNotFoundException | PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to add a moderator to a community.
     */
    @PostMapping("add_moderator/community/{community_id}")
    public ResponseEntity<?> addModerator(@PathVariable(name = "community_id") Long community_id,
                                          @RequestBody AddModeratorRequest addModeratorRequest,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommunityWithAdditionalDataResponse response = moderationService.addModerator(community_id, addModeratorRequest, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * This method handles the POST request to add multiple moderators to a community.
     */
    @PostMapping("/add_moderators/community/{community_id}")
    public ResponseEntity<?> addModerators(@PathVariable Long community_id,
                                          @RequestBody AddModeratorsToCommunityRequest moderators,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            CommunityWithAdditionalDataResponse response = moderationService.addModerators(community_id, moderators, username);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(e.getMessage());
        }
    }

    /**
     * This method handles the DELETE request to remove a user from Redis for a specific community.
     */
    @DeleteMapping("/community/{community_id}/user/{user_id}/delete_from_redis")
    public ResponseEntity<String> deleteFromRedis(@PathVariable("community_id") Long communityId,
                                                  @PathVariable("user_id") Long userId) {
        long isDeleted = moderationService.deleteUserFromRedis(communityId, userId);
        if (isDeleted == 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("not deleted");
        return ResponseEntity.status(HttpStatus.OK).body("deleted");
    }

    @DeleteMapping("/community/{communityId}/post/{postId}/delete_post_from_redis")
    public ResponseEntity<String> deleteFromRedis(@PathVariable("communityId") Long communityId,
                                                  @PathVariable("postId") String postId) {
        long isDeleted = moderationService.deletePostFromRedis(communityId, postId);
        if (isDeleted == 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("not deleted");
        return ResponseEntity.status(HttpStatus.OK).body("deleted");
    }

}
