package com.amalitech.communityboard.mapping;

import com.amalitech.communityboard.dto.response.SubscriptionResponse;
import com.amalitech.communityboard.models.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    SubscriptionResponse toResponse(Subscription subscription);
}

