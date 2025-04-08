package com.posts.post_platform.dto;

import com.posts.post_platform.model.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectJoinRequestDto {
    private Long userId;
    private Long communityId;
    private Long rejecterId;
    private RequestStatus requestStatus;
    private LocalDateTime rejectedAt;
}
