package com.amalitech.communityboard.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostFilter {
    private String title;
    private String content;
    private Long categoryId;
    private Long authorId;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private Integer minViews;
    private Integer maxViews;
}

