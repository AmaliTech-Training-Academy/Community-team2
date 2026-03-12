package com.amalitech.communityboard.dto.request;

import com.amalitech.communityboard.dto.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 3, message = "username cannot be less than 3 letters")
    private String username;

    @Email
    private String email;

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters long and contain both letters and numbers"
    )
    private String password;

    private UserRole role;

}

