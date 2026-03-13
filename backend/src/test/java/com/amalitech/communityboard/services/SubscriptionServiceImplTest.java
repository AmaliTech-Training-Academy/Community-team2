package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.models.Category;
import com.amalitech.communityboard.models.Subscription;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.CategoryRepository;
import com.amalitech.communityboard.repository.SubscriptionRepository;
import com.amalitech.communityboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionServiceImpl Unit Tests")
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User sampleUser;
    private Category sampleCategory;
    private Subscription sampleSubscription;

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
    }

    // ─────────────────────────────────────────────────────────────
    // SUBSCRIBE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        @DisplayName("creates and saves a new subscription when none exists")
        void subscribe_noExisting_createsNewSubscription() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(sampleSubscription);

            Subscription result = subscriptionService.subscribe(sampleUser, sampleCategory);

            assertThat(result).isEqualTo(sampleSubscription);
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("saves new subscription with correct user and category")
        void subscribe_noExisting_setsUserAndCategoryOnNewSubscription() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(sampleSubscription);

            subscriptionService.subscribe(sampleUser, sampleCategory);

            ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(sampleUser);
            assertThat(captor.getValue().getCategory()).isEqualTo(sampleCategory);
        }

        @Test
        @DisplayName("returns existing subscription without saving when already subscribed")
        void subscribe_alreadyExists_returnsExistingWithoutSaving() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.of(sampleSubscription));

            Subscription result = subscriptionService.subscribe(sampleUser, sampleCategory);

            assertThat(result).isEqualTo(sampleSubscription);
            // idempotent — no new record saved
            verify(subscriptionRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UNSUBSCRIBE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("deletes subscription when it exists")
        void unsubscribe_exists_deletesSubscription() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.of(sampleSubscription));
            doNothing().when(subscriptionRepository).delete(sampleSubscription);

            subscriptionService.unsubscribe(sampleUser, sampleCategory);

            verify(subscriptionRepository).delete(sampleSubscription);
        }

        @Test
        @DisplayName("deletes the exact subscription object returned by the repository")
        void unsubscribe_deletesCorrectSubscription() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.of(sampleSubscription));

            subscriptionService.unsubscribe(sampleUser, sampleCategory);

            ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository).delete(captor.capture());
            assertThat(captor.getValue()).isEqualTo(sampleSubscription);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when subscription not found")
        void unsubscribe_notFound_throwsEntityNotFoundException() {
            when(subscriptionRepository.findByUserAndCategory(sampleUser, sampleCategory))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> subscriptionService.unsubscribe(sampleUser, sampleCategory));

            verify(subscriptionRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USER SUBSCRIPTIONS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserSubscriptions")
    class GetUserSubscriptions {

        @Test
        @DisplayName("returns all subscriptions for the given user")
        void getUserSubscriptions_returnsSubscriptions() {
            Subscription second = Subscription.builder()
                    .user(sampleUser).category(new Category()).build();
            when(subscriptionRepository.findByUser(sampleUser))
                    .thenReturn(List.of(sampleSubscription, second));

            List<Subscription> result = subscriptionService.getUserSubscriptions(sampleUser);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(sampleSubscription, second);
        }

        @Test
        @DisplayName("returns empty list when user has no subscriptions")
        void getUserSubscriptions_noSubscriptions_returnsEmptyList() {
            when(subscriptionRepository.findByUser(sampleUser)).thenReturn(List.of());

            List<Subscription> result = subscriptionService.getUserSubscriptions(sampleUser);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET CATEGORY SUBSCRIPTIONS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCategorySubscriptions")
    class GetCategorySubscriptions {

        @Test
        @DisplayName("returns all subscriptions for the given category")
        void getCategorySubscriptions_returnsSubscriptions() {
            User secondUser = new User();
            secondUser.setId(20L);
            Subscription second = Subscription.builder()
                    .user(secondUser).category(sampleCategory).build();

            when(subscriptionRepository.findByCategory(sampleCategory))
                    .thenReturn(List.of(sampleSubscription, second));

            List<Subscription> result = subscriptionService.getCategorySubscriptions(sampleCategory);

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(sampleSubscription, second);
        }

        @Test
        @DisplayName("returns empty list when no one is subscribed to the category")
        void getCategorySubscriptions_noSubscribers_returnsEmptyList() {
            when(subscriptionRepository.findByCategory(sampleCategory)).thenReturn(List.of());

            List<Subscription> result = subscriptionService.getCategorySubscriptions(sampleCategory);

            assertThat(result).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USERS WITH ACTIVE DAILY RECAP
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUsersWithActiveDailyRecap")
    class GetUsersWithActiveDailyRecap {

        @Test
        @DisplayName("returns users who have daily recap enabled")
        void getUsersWithActiveDailyRecap_returnsUsers() {
            when(subscriptionRepository.findDistinctUsersWithActiveDailyRecap())
                    .thenReturn(List.of(sampleUser));

            List<User> result = subscriptionService.getUsersWithActiveDailyRecap();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("silas_dev");
        }

        @Test
        @DisplayName("returns empty list when no users have daily recap enabled")
        void getUsersWithActiveDailyRecap_noUsers_returnsEmptyList() {
            when(subscriptionRepository.findDistinctUsersWithActiveDailyRecap()).thenReturn(List.of());

            List<User> result = subscriptionService.getUsersWithActiveDailyRecap();

            assertThat(result).isEmpty();
        }
    }
}