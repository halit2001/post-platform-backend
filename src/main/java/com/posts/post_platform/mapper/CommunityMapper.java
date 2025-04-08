package com.posts.post_platform.mapper;

import com.posts.post_platform.dto.UserDto;
import com.posts.post_platform.model.AccessLevel;
import com.posts.post_platform.model.Community;
import com.posts.post_platform.model.User;
import com.posts.post_platform.requests.CommunityRequest;
import com.posts.post_platform.response.CommunityResponse;
import com.posts.post_platform.response.CommunityResponseWithApprovedUsers;
import com.posts.post_platform.response.CommunityWithAdditionalDataResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommunityMapper {
    public CommunityResponse convertCommunityToResponse(Community community) {
        return CommunityResponse.builder()
                .community_name(community.getCommunityName())
                .creatorId(community.getCreator().getId())
                .description(community.getDescription())
                .createdAt(community.getCreatedAt())
                .topics(community.getTopics())
                .access_level(community.getAccess_level().name())
                .build();
    }

    public Community convertCommunityRequestToModel(CommunityRequest communityRequest, User user) {
        return Community.builder()
                .creator(user)
                .communityName(communityRequest.getCommunity_name())
                .description(communityRequest.getDescription())
                .topics(communityRequest.getTopics())
                .access_level(AccessLevel.fromString(communityRequest.getAccess_level()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public CommunityWithAdditionalDataResponse convertCommunityToCommunityResponseWithAdditionalData(Community community) {
        return CommunityWithAdditionalDataResponse
                .builder()
                .community_name(community.getCommunityName())
                .creatorId(community.getCreator().getId())
                .description(community.getDescription())
                .createdAt(community.getCreatedAt())
                .topics(community.getTopics())
                .access_level(community.getAccess_level().name())
                .approvedUsers(community.getApprovedUsers())
                .moderators(community.getModerators())
                .posts(community.getPosts())
                .build();
    }

    public CommunityResponseWithApprovedUsers convertCommunityToResponseWithApprovedUsers(Community community) {
        List<UserDto> userDtoList = community.getApprovedUsers().stream().map(user -> UserDto.builder().email(user.getEmail()).username(user.getUsername()).createdAt(user.getCreatedAt()).role(user.getRole()).build()).toList();
        return CommunityResponseWithApprovedUsers.builder()
                .community_name(community.getCommunityName())
                .creatorId(community.getCreator().getId())
                .description(community.getDescription())
                .createdAt(community.getCreatedAt())
                .topics(community.getTopics())
                .access_level(community.getAccess_level().name())
                .approvedUsers(userDtoList)
                .build();
    }
}
