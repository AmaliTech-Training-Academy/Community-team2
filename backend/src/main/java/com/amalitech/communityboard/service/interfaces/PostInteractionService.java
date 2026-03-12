package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PostInteractionService {

    void recordView(User user, Post post);

    void recordComment(User user, Post post);

    void recordLike(User user, Post post);

    Map<Category, List<PostScore>> getTopPostsByCategoryForDate(LocalDate date, int limitPerCategory);

    class PostScore {
        private final Post post;
        private final long views;
        private final long comments;
        private final long likes;
        private final long score;

        public PostScore(Post post, long views, long comments, long likes, long score) {
            this.post = post;
            this.views = views;
            this.comments = comments;
            this.likes = likes;
            this.score = score;
        }

        public Post getPost() {
            return post;
        }

        public long getViews() {
            return views;
        }

        public long getComments() {
            return comments;
        }

        public long getLikes() {
            return likes;
        }

        public long getScore() {
            return score;
        }
    }
}

