// test/java/com/amalitech/communityboard/util/TestUserFactory.java
package com.amalitech.communityboard.util;

import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;

public class TestUserFactory {

    public static CustomUserDetails createAdminUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@test.com");
        user.setPassword("password");
        user.setRole(UserRole.ADMIN);
        user.setCreatedAt(LocalDateTime.now());

        return new CustomUserDetails(
                user,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    public static CustomUserDetails createMemberUser() {
        User user = new User();
        user.setId(2L);
        user.setEmail("member@test.com");
        user.setPassword("password");
        user.setRole(UserRole.MEMBER);
        user.setCreatedAt(LocalDateTime.now());


        return new CustomUserDetails(
                user,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    public static CustomUserDetails createMemberUserWithId(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("member" + id + "@test.com");
        user.setPassword("password");
        user.setRole(UserRole.MEMBER);
        user.setCreatedAt(LocalDateTime.now());

        return new CustomUserDetails(
                user,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }
}