package com.amalitech.communityboard.models;


import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Builder
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Name must contain only letters and numbers")
    private String username;

    @Email
    @Column(unique = true)
    private String email;

    @Size(min = 8, message = "password should not be less than 8 characters")
    @NotBlank(message = "password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.MEMBER;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AccountProvider provider = AccountProvider.LOCAL;

    public User() {}

    public User(String username, String email, String password, UserRole userRole, AccountProvider accountProvider) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = userRole;
        this.provider = accountProvider;
    }
}
