package com.amalitech.communityboard.mapping;

import com.amalitech.communityboard.dto.request.CommentRequest;
import com.amalitech.communityboard.dto.response.CommentResponse;
import com.amalitech.communityboard.models.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "userId",source = "user.id")
    @Mapping(target = "postId",source = "post.id")
    @Mapping(target = "parentCommentId",source = "parent.id")
    CommentResponse toResponse(Comment comment);
    Comment toEntity(CommentRequest comment);

}
