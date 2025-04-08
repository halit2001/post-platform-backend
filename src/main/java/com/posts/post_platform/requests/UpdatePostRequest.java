package com.posts.post_platform.requests;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    private String content;
    private String title;
}
