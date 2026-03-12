package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Post;
import com.amalitech.communityboard.models.PostInteraction;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.PostInteractionRepository;
import com.amalitech.communityboard.service.interfaces.PostInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostInteractionServiceImpl implements PostInteractionService {

    private final PostInteractionRepository interactionRepository;

    @Override
    public void recordView(User user, Post post) {
        saveInteraction(user, post, PostInteraction.InteractionType.VIEW);
    }

    @Override
    public void recordComment(User user, Post post) {
        saveInteraction(user, post, PostInteraction.InteractionType.COMMENT);
    }

    @Override
    public void recordLike(User user, Post post) {
        saveInteraction(user, post, PostInteraction.InteractionType.LIKE);
    }

    private void saveInteraction(User user, Post post, PostInteraction.InteractionType type) {
        PostInteraction interaction = PostInteraction.builder()
                .user(user)
                .post(post)
                .type(type)
                .build();
        interactionRepository.save(interaction);
    }

    @Override
    public Map<Category, List<PostScore>> getTopPostsByCategoryForDate(LocalDate date, int limitPerCategory) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<PostInteraction> interactions = interactionRepository.findByOccurredAtBetween(start, end);

        Map<Post, List<PostInteraction>> byPost = interactions.stream()
                .collect(Collectors.groupingBy(PostInteraction::getPost));

        Map<Category, List<PostScore>> result = new HashMap<>();

        for (Map.Entry<Post, List<PostInteraction>> entry : byPost.entrySet()) {
            Post post = entry.getKey();
            List<PostInteraction> postInteractions = entry.getValue();

            long views = postInteractions.stream().filter(i -> i.getType() == PostInteraction.InteractionType.VIEW).count();
            long comments = postInteractions.stream().filter(i -> i.getType() == PostInteraction.InteractionType.COMMENT).count();
            long likes = postInteractions.stream().filter(i -> i.getType() == PostInteraction.InteractionType.LIKE).count();

            long score = views + comments * 3 + likes * 2;

            Category category = post.getCategory();
            PostScore postScore = new PostScore(post, views, comments, likes, score);

            result.computeIfAbsent(category, c -> new ArrayList<>()).add(postScore);
        }

        result.replaceAll((category, scores) -> scores.stream()
                .sorted(Comparator.comparingLong(PostScore::getScore).reversed())
                .limit(limitPerCategory)
                .collect(Collectors.toList()));

        return result;
    }
}

