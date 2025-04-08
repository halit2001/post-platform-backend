package com.posts.post_platform.controller;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.exceptions.CommunityAlreadyExistsException;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.requests.CommunityRequest;
import com.posts.post_platform.requests.UpdateCommunityRequest;
import com.posts.post_platform.response.CommunityResponse;
import com.posts.post_platform.response.CommunityResponseWithApprovedUsers;
import com.posts.post_platform.service.RedisService;
import com.posts.post_platform.service.community.CommunityService;
import com.posts.post_platform.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/community")
public class CommunityController {
    private final CommunityService communityService;
    private final RedisService redisService;
    private final UserService userService;

    @Autowired
    public CommunityController(CommunityService communityService, RedisService redisService, UserService userService) {
        this.communityService = communityService;
        this.redisService = redisService;
        this.userService = userService;
    }

    /**
     * Get community details by ID.
     * @param community_id The ID of the community.
     * @return Community details in the response body.
     */
    @GetMapping("/get_community/{community_id}")
    public ResponseEntity<?> getCommunity(@PathVariable Long community_id) throws Exception{
        try {
            CommunityResponse communityResponse = communityService.getCommunity(community_id);
            return ResponseEntity.status(HttpStatus.OK).body(communityResponse);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    /**
     * Get community details by name.
     * @param community_name The name of the community.
     * @return Community details in the response body.
     */
    @GetMapping("/get_community_by_name/{community_name}")
    public ResponseEntity<?> getCommunityByName(@PathVariable String community_name) throws Exception{
        try {
            CommunityResponse communityResponse = communityService.getCommunityByName(community_name);
            return ResponseEntity.status(HttpStatus.OK).body(communityResponse);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    /**
     * Get all members of a specific community.
     * @param community_id The ID of the community.
     * @return A list of users in the community.
     */
    @GetMapping("/get_all_community_members/{community_id}")
    public ResponseEntity<?> getAllMembers(@PathVariable Long community_id) throws Exception{
        try {
            List<UserDto> userList = communityService.getAllMembers(community_id);
            if (userList.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Members not found");
            return ResponseEntity.status(HttpStatus.OK).body(userList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get the count of members in a community.
     * @param community_id The ID of the community.
     * @return The number of members in the community.
     */
    @GetMapping("/find_members_count/{community_id}")
    public ResponseEntity<?> getMembersCount(@PathVariable Long community_id) throws Exception{
        try {
            int membersCount = communityService.getMembersCount(community_id);
            if(membersCount == 0) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Members count is : " + membersCount);
            return ResponseEntity.status(HttpStatus.OK).body("Members count is : " + membersCount);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    /**
     * Get all communities.
     * @return A list of all communities.
     */
    @GetMapping("/get_all_communities")
    public ResponseEntity<?> getAllCommunities() throws Exception{
        try {
            List<CommunityResponse> communityResponseList = communityService.getAllCommunities();
            return ResponseEntity.status(HttpStatus.OK).body(communityResponseList);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    /**
     * Request to join a community.
     * @param community_id The ID of the community.
     * @param userDetails The authenticated user requesting to join.
     * @return A response indicating the result of the request.
     */
    @PostMapping("/request_to_join_community/community_id/{community_id}")
    public ResponseEntity<?> requestToJoinCommunity(@PathVariable(name = "community_id") Long community_id, @AuthenticationPrincipal UserDetails userDetails) {
        Community community = communityService.findCommunityById(community_id).orElseThrow(() -> new CommunityNotFoundException("Community not found with id : " + community_id));
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            if (!community.isPrivate()) {
                CommunityResponseWithApprovedUsers response = communityService.addUserToCommunity(community, username);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                Long userId = userService.getUserId(username);
                boolean added = redisService.saveJoinRequest(community_id, userId, username);
                if (!added) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Join request already exists!");
                }
                return ResponseEntity.status(HttpStatus.CREATED).body("Join request submitted.");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedActionException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * Create a new community.
     * @param communityRequest The request body containing community details.
     * @param userDetails The authenticated user creating the community.
     * @return The created community response.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createCommunity(@Valid @RequestBody CommunityRequest communityRequest, @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            CommunityResponse communityResponse = communityService.createCommunity(communityRequest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(communityResponse);
        } catch (UserNotFoundException | CommunityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Update a community by its name.
     * @param updateCommunityRequest The request body containing updated community details.
     * @param community_name The name of the community to update.
     * @param userDetails The authenticated user performing the update.
     * @return The updated community response.
     */
    @PatchMapping("/update_community/{community_name}")
    public ResponseEntity<?> updateCommunity(@Valid @RequestBody UpdateCommunityRequest updateCommunityRequest, @PathVariable String community_name, @AuthenticationPrincipal UserDetails userDetails) throws Exception{
        try {
            String username = userService.getUsernameFromAuthentication(userDetails);
            if (username == null) throw new UnauthorizedActionException("User is not authenticated");
            CommunityResponse communityResponse = communityService.updateCommunity(community_name, updateCommunityRequest, username);
            return ResponseEntity.status(HttpStatus.OK).body(communityResponse);
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (CommunityNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
