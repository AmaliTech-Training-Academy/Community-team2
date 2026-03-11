package com.amalitech.communityboard.dto.request;

import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserFilter {
    private String username;
    private String email;
    private UserRole role;
    private AccountProvider provider;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}

