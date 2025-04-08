package com.posts.post_platform.service.community;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.requests.CommunityRequest;
import com.posts.post_platform.requests.UpdateCommunityRequest;
import com.posts.post_platform.response.CommunityResponse;
import com.posts.post_platform.response.CommunityResponseWithApprovedUsers;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface CommunityService {
    CommunityResponse createCommunity(@Valid CommunityRequest communityRequest, String username);

    CommunityResponse getCommunity(Long communityId);

    CommunityResponse getCommunityByName(String communityName);

    List<CommunityResponse> getAllCommunities();

    CommunityResponse updateCommunity(String communityName, @Valid UpdateCommunityRequest updateCommunityRequest, String username);

    List<UserDto> getAllMembers(Long communityId);

    int getMembersCount(Long communityId);

    Optional<Community> findCommunityById(Long communityId);

    CommunityResponseWithApprovedUsers addUserToCommunity(Community community, String username);

    boolean isCreator(Long communityId, String username);
    boolean isMember(String communityName, Long userId);

    boolean isModerator(Long communityId, String username);

    boolean isCommunityPrivate(String communityName);
}
