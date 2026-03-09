package com.amalitech.communityboard.dto.request;

import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotBlank
    @Size(min = 3,message = "username cannot be less than 3 letters")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8,message = "password cannot be less than 8 characters")
    private String password;

    private UserRole role;

    private AccountProvider provider;
}
