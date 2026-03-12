package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.SubscriptionService;
import com.amalitech.communityboard.dto.response.SubscriptionResponse;
import com.amalitech.communityboard.mapping.SubscriptionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Category subscription endpoints")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  UserRepository userRepository,
                                  CategoryRepository categoryRepository,
                                  SubscriptionMapper subscriptionMapper) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.subscriptionMapper = subscriptionMapper;
    }

    @PostMapping("/categories/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Subscribe to a category")
    public ResponseDto<SubscriptionResponse> subscribe(@PathVariable Long categoryId,
                                               @AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        Subscription subscription = subscriptionService.subscribe(user, category);
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);
        return new ResponseDto<>(HttpStatus.CREATED, "subscribed to category", response);
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Unsubscribe from a category")
    public ResponseDto<Void> unsubscribe(@PathVariable Long categoryId,
                                         @AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        subscriptionService.unsubscribe(user, category);
        return new ResponseDto<>(HttpStatus.NO_CONTENT, "unsubscribed from category", null);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's subscriptions")
    public ResponseDto<List<SubscriptionResponse>> getUserSubscriptions(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
        List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user);
        List<SubscriptionResponse> responses = subscriptions.stream()
                .map(subscriptionMapper::toResponse)
                .toList();
        return new ResponseDto<>(HttpStatus.OK, "subscriptions retrieved", responses);
    }
}
