package com.amalitech.communityboard.dto.request;

import io.swagger.v3.oas.annotations.info.Info;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
