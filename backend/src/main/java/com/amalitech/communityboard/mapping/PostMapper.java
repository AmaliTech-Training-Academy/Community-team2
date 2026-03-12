package com.amalitech.communityboard.mapping;

import com.amalitech.communityboard.dto.request.PostRequest;
import com.amalitech.communityboard.dto.response.PostResponse;
import com.amalitech.communityboard.models.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "userId", source = "author.id")
     PostResponse toResponse(Post save);

    Post toEntity(PostRequest post);
}
