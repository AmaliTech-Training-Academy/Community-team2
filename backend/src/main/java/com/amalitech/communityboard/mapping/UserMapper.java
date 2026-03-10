package com.amalitech.communityboard.mapping;

import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.models.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest user);
    List<UserResponse> toResponse(List<User> dtoList);

}
