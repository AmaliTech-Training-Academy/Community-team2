package com.amalitech.communityboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
    private Long categoryId;
    private String imageUrl;
    private int viewCount = 0;

}
