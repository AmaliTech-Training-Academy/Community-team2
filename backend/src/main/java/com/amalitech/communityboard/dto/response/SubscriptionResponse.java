package com.amalitech.communityboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private boolean immediateNotificationsEnabled;
    private boolean dailyRecapEnabled;
    private boolean muted;
}

