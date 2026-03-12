package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.response.SubscriptionResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.SubscriptionMapper;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.service.interfaces.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionController Unit Tests")
class SubscriptionControllerTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private CustomUserDetails principal;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private User sampleUser;
    private Category sampleCategory;
    private Subscription sampleSubscription;
    private SubscriptionResponse sampleSubscriptionResponse;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setUsername("silas_dev");

        sampleCategory = new Category();
        sampleCategory.setId(2L);
        sampleCategory.setName("Events");

        sampleSubscription = Subscription.builder()
                .user(sampleUser)
                .category(sampleCategory)
                .build();

        sampleSubscriptionResponse = SubscriptionResponse.builder()
                .id(1L)
                .categoryId(2L)
                .categoryName("Events")
                .immediateNotificationsEnabled(true)
                .dailyRecapEnabled(false)
                .muted(false)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // SUBSCRIBE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /categories/{categoryId} – subscribe")
    class Subscribe {

        @Test
        @DisplayName("returns 201 with subscription response on valid subscribe")
        void subscribe_valid_returns201() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            when(subscriptionService.subscribe(sampleUser, sampleCategory)).thenReturn(sampleSubscription);
            when(subscriptionMapper.toResponse(sampleSubscription)).thenReturn(sampleSubscriptionResponse);

            ResponseDto<SubscriptionResponse> response = subscriptionController.subscribe(2L, principal);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.message()).isEqualTo("subscribed to category");
            assertThat(response.data().getCategoryId()).isEqualTo(2L);
            assertThat(response.data().getCategoryName()).isEqualTo("Events");
            verify(subscriptionService).subscribe(sampleUser, sampleCategory);
        }

        @Test
        @DisplayName("returns existing subscription when already subscribed (idempotent)")
        void subscribe_alreadySubscribed_returnsExisting() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            // Service is idempotent — returns the existing one
            when(subscriptionService.subscribe(sampleUser, sampleCategory)).thenReturn(sampleSubscription);
            when(subscriptionMapper.toResponse(sampleSubscription)).thenReturn(sampleSubscriptionResponse);

            ResponseDto<SubscriptionResponse> response = subscriptionController.subscribe(2L, principal);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.data()).isEqualTo(sampleSubscriptionResponse);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when authenticated user not found")
        void subscribe_userNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(99L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.subscribe(2L, principal));

            verifyNoInteractions(categoryRepository, subscriptionService);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when category not found")
        void subscribe_categoryNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.subscribe(99L, principal));

            verifyNoInteractions(subscriptionService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UNSUBSCRIBE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /categories/{categoryId} – unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("returns 204 with null data on successful unsubscribe")
        void unsubscribe_valid_returns204() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            doNothing().when(subscriptionService).unsubscribe(sampleUser, sampleCategory);

            ResponseDto<Void> response = subscriptionController.unsubscribe(2L, principal);

            assertThat(response.status()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.message()).isEqualTo("unsubscribed from category");
            assertThat(response.data()).isNull();
            verify(subscriptionService).unsubscribe(sampleUser, sampleCategory);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void unsubscribe_userNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(99L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.unsubscribe(2L, principal));

            verifyNoInteractions(categoryRepository, subscriptionService);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when category not found")
        void unsubscribe_categoryNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.unsubscribe(99L, principal));

            verifyNoInteractions(subscriptionService);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when subscription not found during unsubscribe")
        void unsubscribe_subscriptionNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(sampleCategory));
            doThrow(new EntityNotFoundException("subscription not found"))
                    .when(subscriptionService).unsubscribe(sampleUser, sampleCategory);

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.unsubscribe(2L, principal));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USER SUBSCRIPTIONS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET / – getUserSubscriptions")
    class GetUserSubscriptions {

        @Test
        @DisplayName("returns 200 with list of subscription responses")
        void getUserSubscriptions_valid_returnsSubscriptions() {
            SubscriptionResponse secondResponse = SubscriptionResponse.builder()
                    .id(2L).categoryId(3L).categoryName("Announcements").build();
            Subscription secondSubscription = Subscription.builder()
                    .user(sampleUser).category(new Category()).build();

            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(subscriptionService.getUserSubscriptions(sampleUser))
                    .thenReturn(List.of(sampleSubscription, secondSubscription));
            when(subscriptionMapper.toResponse(sampleSubscription)).thenReturn(sampleSubscriptionResponse);
            when(subscriptionMapper.toResponse(secondSubscription)).thenReturn(secondResponse);

            ResponseDto<List<SubscriptionResponse>> response =
                    subscriptionController.getUserSubscriptions(principal);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("subscriptions retrieved");
            assertThat(response.data()).hasSize(2);
            assertThat(response.data().get(0).getCategoryName()).isEqualTo("Events");
            assertThat(response.data().get(1).getCategoryName()).isEqualTo("Announcements");
        }

        @Test
        @DisplayName("returns empty list when user has no subscriptions")
        void getUserSubscriptions_noSubscriptions_returnsEmptyList() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(subscriptionService.getUserSubscriptions(sampleUser)).thenReturn(List.of());

            ResponseDto<List<SubscriptionResponse>> response =
                    subscriptionController.getUserSubscriptions(principal);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.data()).isEmpty();
            verifyNoInteractions(subscriptionMapper);
        }

        @Test
        @DisplayName("maps each subscription through the mapper")
        void getUserSubscriptions_callsMapperForEachSubscription() {
            when(principal.getId()).thenReturn(10L);
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(subscriptionService.getUserSubscriptions(sampleUser))
                    .thenReturn(List.of(sampleSubscription, sampleSubscription));
            when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(sampleSubscriptionResponse);

            subscriptionController.getUserSubscriptions(principal);

            verify(subscriptionMapper, times(2)).toResponse(sampleSubscription);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void getUserSubscriptions_userNotFound_throwsEntityNotFoundException() {
            when(principal.getId()).thenReturn(99L);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionController.getUserSubscriptions(principal));

            verifyNoInteractions(subscriptionService, subscriptionMapper);
        }
    }
}