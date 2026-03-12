package com.amalitech.communityboard.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotNull
    private Long postId;

    @NotBlank
    private String content;

    @Nullable
    private Long parentCommentId;
}
