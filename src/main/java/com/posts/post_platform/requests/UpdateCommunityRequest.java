package com.posts.post_platform.requests;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityRequest {

    @Size(min = 3, max = 21 , message = "Community name must be between 3 and 21")
    private String community_name;

    @Size(max = 160 , message = "Community name must be at most 160 characters")
    private String description;
    private List<String> topics;
    private String access_level;
}
