package com.amalitech.communityboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull(message = "categoryId cannot be null")
    private Long categoryId;


}
