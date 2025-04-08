package com.posts.post_platform.mapper;

import com.posts.post_platform.dto.ApproveJoinRequestDto;
import com.posts.post_platform.dto.RejectJoinRequestDto;
import com.posts.post_platform.model.RequestStatus;
import com.posts.post_platform.response.PendingPost;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ModerationMapper {

    public ApproveJoinRequestDto createApproveJoinRequestDto(Long userId, Long approverId, Long communityId) {
        return ApproveJoinRequestDto.builder()
                .userId(userId)
                .communityId(communityId)
                .approverId(approverId)
                .approvedAt(LocalDateTime.now())
                .requestStatus(RequestStatus.APPROVED)
                .build();
    }

    public RejectJoinRequestDto createRejectJoinRequestDto(Long userToAttendId, Long rejecterId, Long communityId) {
        return RejectJoinRequestDto.builder()
                .userId(userToAttendId)
                .rejecterId(rejecterId)
                .communityId(communityId)
                .requestStatus(RequestStatus.REJECTED)
                .rejectedAt(LocalDateTime.now())
                .build();
    }

    public PendingPost convertPendingPostToPostResponse(JSONObject jsonObject, String communityName) {
        return PendingPost.builder()
                .post_id(UUID.fromString(jsonObject.get("post_id").toString()).toString())
                .title(jsonObject.getString("title"))
                .content(jsonObject.getString("content"))
                .createdAt(LocalDateTime.parse(jsonObject.get("requested_at").toString()))
                .status(jsonObject.get("status").toString())
                .creatorUsername(jsonObject.getString("creator_username"))
                .communityName(communityName)
                .build();
    }
}
