package com.amalitech.communityboard.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "PostFilter{" +
                "authorId=" + authorId +
                ", categoryId=" + categoryId +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostFilter)) {
            return false;
        }
        PostFilter that = (PostFilter) o;
        return Objects.equals(authorId, that.authorId) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorId, categoryId, title);
    }
}

