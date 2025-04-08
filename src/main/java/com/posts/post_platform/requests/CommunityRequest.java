package com.posts.post_platform.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRequest {
    @NotNull
    @Size(min = 3, max = 21 , message = "Community name must be between 3 and 21")
    private String community_name;

    @NotNull(message = "Description cannot be null.")
    @NotBlank(message = "Description cannot be empty.")
    @Size(max = 120 , message = "Community name must be at most 160 characters")
    private String description;

    private List<String> topics;

    @NotNull(message = "Access level cannot be null.")
    private String access_level;
}
