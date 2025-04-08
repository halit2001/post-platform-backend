package com.posts.post_platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class PostDto {
    @NotNull
    @Size(min = 10, message = "Min is 10 word")
    private String title;
    @NotNull
    private String content;
}
