package com.amalitech.communityboard.models;


import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "username cannot be empty")
    @Size(min = 2,message = "username cannot be less than 2 letters")
    private String username;

    @Email
    @Column(unique = true)
    private String email;

    @Size(min = 8,message = "password should not be less than 8 characters")
    @NotBlank(message = "password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.MEMBER;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AccountProvider provider = AccountProvider.LOCAL;

    public User() {}
}
