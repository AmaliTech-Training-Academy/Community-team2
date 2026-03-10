package com.amalitech.communityboard.dto.response;

import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private AccountProvider provider;
    private LocalDateTime createdAt;
}
