package com.posts.post_platform.service.moderation;

import com.posts.post_platform.dto.ApproveJoinRequestDto;
import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.dto.RejectJoinRequestDto;
import com.posts.post_platform.exceptions.CommunityNotFoundException;
import com.posts.post_platform.exceptions.PostNotFoundException;
import com.posts.post_platform.exceptions.UnauthorizedActionException;
import com.posts.post_platform.exceptions.UserNotFoundException;
import com.posts.post_platform.mapper.CommunityMapper;
import com.posts.post_platform.mapper.ModerationMapper;
import com.posts.post_platform.mapper.PostMapper;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.Post;
import com.posts.post_platform.model.Role;
import com.posts.post_platform.model.User;
import com.posts.post_platform.repository.CommunityRepository;
import com.posts.post_platform.repository.PostRepository;
import com.posts.post_platform.repository.UserRepositories;
import com.posts.post_platform.requests.AddModeratorRequest;
import com.posts.post_platform.requests.AddModeratorsToCommunityRequest;
import com.posts.post_platform.response.CommunityWithAdditionalDataResponse;
import com.posts.post_platform.response.PendingPost;
import com.posts.post_platform.response.PostResponse;
import com.posts.post_platform.service.RedisService;
import com.posts.post_platform.service.community.CommunityService;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ModerationServiceImpl implements ModerationService{
    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private CommunityMapper communityMapper;

    @Autowired
    private ModerationMapper moderationMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private RedisService redisService;

    /**
     * This method allows the creator of a community to add multiple users as moderators.
     * It verifies the user's authorization, checks the community's approved users,
     * and adds users to the moderator list if they are eligible. It also ensures that
     * duplicate users or users who are not in the approved list are not added as moderators.
     * If no users are added, an exception is thrown. Finally, the community is saved and
     * a response with the updated community data is returned.
     */
    @Override
    @Transactional
    public CommunityWithAdditionalDataResponse addModerators(Long communityId, AddModeratorsToCommunityRequest addToModerators, String username) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException(("Community not found with id : " + communityId )));
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        if (!community.getCreator().equals(user)) throw new UnauthorizedActionException("User is not the creator of this community");
        List<Long> approvedUserIds = community.getApprovedUsers().stream().map(User::getId).toList();
        List<Long> filteredUserIds = addToModerators.getUser_ids().stream().filter(approvedUserIds::contains).toList();
        int countAddedMod = 0;
        for (Long userId : filteredUserIds) {
            Optional<User> userToAddOpt = userRepository.findById(userId);
            if (userToAddOpt.isPresent()) {
                User userToAdd = userToAddOpt.get();
                if (!community.getModerators().contains(userToAdd)) {
                    if (!userToAdd.getRole().contains(Role.MODERATOR)) userToAdd.getRole().add(Role.MODERATOR);
                    community.getModerators().add(userToAdd);
                    userToAdd.getModeratedCommunities().add(community);
                    userRepository.save(userToAdd);
                    countAddedMod++;
                }
            }
        }
        if (countAddedMod == 0) throw new IllegalArgumentException("Any user not added to community");
        communityRepository.save(community);
        return communityMapper.convertCommunityToCommunityResponseWithAdditionalData(community);
    }

    /**
     * This method is used to add a moderator to a community. It checks several conditions before adding the user as a moderator:
     * 1. The community is fetched by its ID. If not found, a CommunityNotFoundException is thrown.
     * 2. The user trying to add a moderator is fetched by their username. If not found, a UserNotFoundException is thrown.
     * 3. The method ensures that the user attempting the action is the creator of the community. If not, an UnauthorizedActionException is thrown.
     * 4. The user to be added as a moderator is fetched by their ID. If the user is already a member of the community and is not a moderator yet,
     *    they are added as a moderator. If successful, the community and user objects are saved, and a response with additional community data is returned.
     * 5. If the user to be added is not part of the community, an IllegalArgumentException is thrown.
     */
    @Override
    @Transactional
    public CommunityWithAdditionalDataResponse addModerator(Long communityId, AddModeratorRequest addModeratorRequest, String username) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException("Community not found with id : " + communityId));
        User user = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username : " + username));
        // we need to check the community creator whether it is equal to user object which is called user
        if (!community.getCreator().equals(user)) throw new UnauthorizedActionException("User is not the creator of this community");
        User userToAdd = userRepository.findById(addModeratorRequest.getUserToModeratorId()).orElseThrow(() -> new UserNotFoundException("User who wants to be added to community as moderator not found with id : " + addModeratorRequest.getUserToModeratorId()));
        if (community.getApprovedUsers().contains(userToAdd) && !community.getModerators().contains(userToAdd)) {
            if (!userToAdd.getRole().contains(Role.MODERATOR)) userToAdd.getRole().add(Role.MODERATOR);
            community.getModerators().add(userToAdd);
            userToAdd.getModeratedCommunities().add(community);
            userRepository.save(userToAdd);
            communityRepository.save(community);
            return communityMapper.convertCommunityToCommunityResponseWithAdditionalData(community);
        }
        throw new IllegalArgumentException("User not belongs to the community");
    }

    /**
     * This method creates a pending post for a community.
     * It saves the post in Redis and returns a `PendingPost` response with additional data.
     *
     * @param community The community where the post is being created.
     * @param postDto The data transfer object containing the post details.
     * @param user The user creating the post.
     * @return A `PendingPost` object containing the post details and the community name.
     */
    @Override
    public PendingPost createPendingPost(Community community, PostDto postDto, User user) {
        JSONObject jsonObject = redisService.savePendingPost(community.getId(), postDto, user);
        return moderationMapper.convertPendingPostToPostResponse(jsonObject, community.getCommunityName());
    }

    /**
     * This method retrieves the pending join requests for a community.
     * It checks if the user has the necessary permissions (either creator or moderator).
     * If authorized, it fetches the pending join requests from Redis.
     *
     * @param communityId The ID of the community.
     * @param username The username of the user requesting the join requests.
     * @return A list of pending join requests.
     * @throws UnauthorizedActionException if the user does not have permission to view the requests.
     * @throws CommunityNotFoundException if the community does not exist.
     * @throws UserNotFoundException if the user does not exist.
     */
    @Override
    public List<Map<String, Object>> getPendingJoinRequests(Long communityId, String username) {
        userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("USER NOT FOUND WITH USERNAME : " + username));
        communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException("COMMUNITY NOT FOUND WITH ID : " + communityId));
        boolean isCreator = communityService.isCreator(communityId, username);
        boolean isModerator = communityService.isModerator(communityId, username);
        if (isCreator || isModerator) {
            List<Map<String, Object>> users = redisService.getPendingJoinRequests(communityId);
            if (!users.isEmpty()) {
                users.forEach(obj -> {
                    Object userId = obj.get("user_id");
                    if (userId instanceof Number) {
                        obj.put("user_id", ((Number) userId).longValue());
                    }
                });
                return users;
            }
            return Collections.emptyList();
        }
        throw new UnauthorizedActionException("You are not allowed to moderate to community with ID: " + communityId);
    }

    /**
     * This method retrieves the pending post requests for a community.
     * It checks if the user has the necessary permissions (either creator or moderator).
     * If authorized, it fetches the pending post requests from Redis.
     *
     * @param communityId The ID of the community.
     * @param username The username of the user requesting the post requests.
     * @return A list of pending post requests.
     * @throws UnauthorizedActionException if the user does not have permission to view the requests.
     * @throws CommunityNotFoundException if the community does not exist.
     * @throws UserNotFoundException if the user does not exist.
     */
    @Override
    public List<Map<String, Object>> getPendingPostRequests(Long communityId, String username) {
        communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException("COMMUNITY NOT FOUND WITH ID : " + communityId));
        userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException(("USER NOT FOUND WITH USERNAME : " + username)));
        if (!communityService.isCreator(communityId, username) && !communityService.isModerator(communityId, username)) {
            throw new UnauthorizedActionException("You are not allowed to moderate to community with ID: " + communityId);
        }
        List<Map<String, Object>> posts = redisService.getAllPendingPostsFromRedis(communityId);
        if (!posts.isEmpty()) {
            posts.forEach(post -> {
                if (post.containsKey("creator_id")) {
                    Object creatorId = post.get("creator_id");
                    if (creatorId instanceof Number) {
                        post.put("creator_id", ((Number) creatorId).longValue());
                    }
                }
            });
            return posts;
        }
        return Collections.emptyList();
    }

    /**
     * This method approves a join request for a user to join the community.
     * It checks if the requesting user is either the creator or a moderator of the community.
     * If the user is already a member, it throws an exception.
     * If the user is not found in Redis, it throws an exception.
     * Upon successful approval, the user is added to the community and the request is removed from Redis.
     **
     * Fetch the approver (user who wants to approve the join request) from the database using their username.
     * If the user is not found, an exception is thrown.
     */
    @Override
    @Transactional
    public ApproveJoinRequestDto approveJoinRequest(Long communityId, Long userId, String username) {
        User approver = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("USER WHO WANTS TO APPROVE NOT FOUND WITH USERNAME : " + username));
        User userToAttend = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("USER WHO WANTS TO ATTEND TO COMMUNITY NOT FOUND WITH ID : " + userId));
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException("COMMUNITY NOT FOUND WITH ID : " + communityId));
        boolean isCreator = communityService.isCreator(communityId, username);
        boolean isModerator = communityService.isModerator(communityId, username);
        if (!isCreator && !isModerator) throw new UnauthorizedActionException("You are not allowed to moderate to community with ID: " + communityId);
        if (community.getApprovedUsers().contains(userToAttend)) throw new EntityExistsException("User is already a member of the community.");
        int index = redisService.findIndex(communityId, userId);
        if (index != -1) {
            community.getApprovedUsers().add(userToAttend);
            communityRepository.save(community);
            redisService.deleteValueFromRedis("community_id:", communityId, index);
            return moderationMapper.createApproveJoinRequestDto(userId, approver.getId(), communityId);
        }
        throw new UserNotFoundException("User not found in redis");
    }

    /**
     * This method approves a post request in a specific community.
     * - It first checks if the community exists in the database.
     * - Then, it validates whether the user has the necessary permissions (creator or moderator).
     * - The post is retrieved from Redis using its postId.
     * - If the post is found in Redis, it is converted into a Post model and saved in the database.
     * - After saving the post, the corresponding post entry is deleted from Redis.
     * - If any part of this process fails (community not found, user permissions not valid, post not found), appropriate exceptions are thrown.
     */
    @Override
    @Transactional
    public PostResponse approvePostRequest(Long communityId, String postId, String username) {
        Community community = communityRepository.findById(communityId).orElseThrow(() ->
                new CommunityNotFoundException("Community not found with id : " + communityId));

        validateUserAndPermissions(communityId, username);

        int index = redisService.findIndexForPost(communityId, postId);
        if (index == -1) throw new PostNotFoundException("Post not found in redis");

        Map<String, Object> post = getPostFromRedis(communityId, index);
        User user = fetchPostCreator(post);

        Post ppost = postMapper.createPostModel(post, user, community);
        postRepository.save(ppost);
        redisService.deleteValueFromRedis("post:community_id:", communityId, index);

        return postMapper.convertPostToPostResponse(ppost);
    }

    /**
     * This method rejects a post request in a specific community.
     * - It first checks if the community exists in the database.
     * - Then, it validates whether the user has the necessary permissions (creator or moderator).
     * - The post is retrieved from Redis using its postId.
     * - If the post is found in Redis, it is deleted from Redis.
     * - If the post is not found, a PostNotFoundException is thrown.
     * - Once the post is deleted from Redis, the method returns a confirmation message "Post deleted."
     */
    @Override
    public String rejectPostRequest(Long communityId, String postId, String username) {
        Community community = communityRepository.findById(communityId).orElseThrow(() ->
                new CommunityNotFoundException("Community not found with id : " + communityId));

        validateUserAndPermissions(communityId, username);

        int index = redisService.findIndexForPost(communityId, postId);
        if (index == -1) throw new PostNotFoundException("Post not found in redis");

        redisService.deleteValueFromRedis("post:community_id:", communityId, index);

        return "Post deleted.";
    }

    /**
     * This method rejects a user's join request to a specific community.
     * - It first checks if the community, the user requesting the rejection, and the user attempting to join are valid.
     * - The user's permissions are validated to ensure the requester is either the creator or a moderator of the community.
     * - The method checks if the user trying to join is already a member of the community; if they are, an exception is thrown.
     * - The join request is found in Redis, and if it's found, the request is deleted from Redis.
     * - If the join request is successfully rejected, a RejectJoinRequestDto is created and returned.
     * - If the user is not found in Redis, a UserNotFoundException is thrown.
     */
    @Override
    public RejectJoinRequestDto rejectJoinRequest(Long communityId, Long userToAttendId, String username) {
        User rUser = userRepository.findUserByUsername(username).orElseThrow(() -> new UserNotFoundException("USER WHO WANTS TO REJECT NOT FOUND WITH USERNAME : " + username));
        User userToAttend = userRepository.findById(userToAttendId).orElseThrow(() -> new UserNotFoundException("USER WHO WANTS TO ATTEND TO COMMUNITY NOT FOUND WITH ID : " + userToAttendId));
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new CommunityNotFoundException("COMMUNITY NOT FOUND WITH ID : " + communityId));
        boolean isCreator = communityService.isCreator(communityId, username);
        boolean isModerator = communityService.isModerator(communityId, username);
        if (!isCreator && !isModerator) throw new UnauthorizedActionException("You are not allowed to moderate to community with ID: " + communityId);
        if (community.getApprovedUsers().contains(userToAttend)) throw new EntityExistsException("User is already a member of the community.");
        int index = redisService.findIndex(communityId, userToAttendId);
        if (index != -1) {
            redisService.deleteValueFromRedis("community_id:", communityId, index);
            return moderationMapper.createRejectJoinRequestDto(userToAttendId, rUser.getId(), communityId);
        }
        throw new UserNotFoundException("User not found in redis");
    }

    @Override
    public long deleteUserFromRedis(Long communityId, Long userId) {
        int index = redisService.findIndex(communityId, userId);
        if (index != -1) {
            return redisService.deleteValueFromRedis("community_id:", communityId, index);
        }
        return 0;
    }

    /**
     * This service handles interactions with Redis, primarily for deleting users and posts from Redis.
     * - `deleteUserFromRedis`: Deletes a user from Redis if they are pending to join the community.
     * - `deletePostFromRedis`: Deletes a post from Redis if it is pending approval.
     * - `getPostFromRedis`: Retrieves a post from Redis given the community ID and index.
     * - `findPostIndex`: Finds the index of a post in Redis by its ID and community ID.
     * - `findUserIndex`: Finds the index of a user in Redis by their ID and community ID.
     * - `validateUserAndPermissions`: Validates the user attempting to approve or moderate a request, ensuring they are either the creator or a moderator of the community.
     * - `fetchPostCreator`: Fetches the user who created a post based on the data stored in Redis.
     */

    @Override
    public long deletePostFromRedis(Long communityId, String postId) {
        int index = redisService.findIndexForPost(communityId, postId);
        if (index == -1) return 0;
        return redisService.deleteValueFromRedis("post:community_id:", communityId, index);
    }

    public Map<String, Object> getPostFromRedis(Long communityId, int index) {
        List<Map<String, Object>> posts = redisService.getAllPendingPostsFromRedis(communityId);
        return posts.get(index);
    }

    @Override
    public int findPostIndex(Long communityId, String postId) {
        return redisService.findIndexForPost(communityId, postId);
    }

    @Override
    public int findUserIndex(Long communityId, Long userId) {
        return redisService.findIndex(communityId, userId);
    }

    private void validateUserAndPermissions(Long communityId, String username) {
        userRepository.findUserByUsername(username).orElseThrow(() ->
                new UserNotFoundException("USER WHO WANTS TO APPROVE NOT FOUND WITH USERNAME : " + username));

        boolean hasPermission = communityService.isCreator(communityId, username) ||
                communityService.isModerator(communityId, username);

        if (!hasPermission) throw new UnauthorizedActionException("You are not allowed to moderate community with ID: " + communityId);
    }

    private User fetchPostCreator(Map<String, Object> post) {
        Long userId = ((Number) post.get("creator_id")).longValue();
        return userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + userId));
    }

}
