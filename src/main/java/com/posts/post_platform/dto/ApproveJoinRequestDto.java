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
public class ApproveJoinRequestDto {
    private Long userId;
    private Long communityId;
    private Long approverId;
    private RequestStatus requestStatus;
    private LocalDateTime approvedAt;

}
