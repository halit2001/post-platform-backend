package com.posts.post_platform.service.moderation;

import com.posts.post_platform.dto.ApproveJoinRequestDto;
import com.posts.post_platform.dto.PostDto;
import com.posts.post_platform.dto.RejectJoinRequestDto;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.AddModeratorRequest;
import com.posts.post_platform.requests.AddModeratorsToCommunityRequest;
import com.posts.post_platform.response.CommunityWithAdditionalDataResponse;
import com.posts.post_platform.response.PendingPost;
import com.posts.post_platform.response.PostResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface ModerationService {
    CommunityWithAdditionalDataResponse addModerators(Long communityId, AddModeratorsToCommunityRequest moderators, String username);

    CommunityWithAdditionalDataResponse addModerator(Long communityId, AddModeratorRequest addModeratorRequest, String username);

    PendingPost createPendingPost(Community community, @Valid PostDto postDto, User user);

    List<Map<String, Object>> getPendingJoinRequests(Long communityId, String username);

    ApproveJoinRequestDto approveJoinRequest(Long communityId, Long userId, String username);

    RejectJoinRequestDto rejectJoinRequest(Long communityId, Long userToAttendId, String username);

    long deleteUserFromRedis(Long communityId, Long userId);

    List<Map<String, Object>> getPendingPostRequests(Long communityId, String username);

    int findPostIndex(Long communityId, String postId);

    int findUserIndex(Long communityId, Long userId);

    long deletePostFromRedis(Long communityId, String postId);

    PostResponse approvePostRequest(Long communityId, String postId, String username);

    String rejectPostRequest(Long communityId, String postId, String username);
}
