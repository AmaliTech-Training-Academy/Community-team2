// test/java/com/amalitech/communityboard/config/TestSecurityConfig.java
package com.amalitech.communityboard.config;

import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.security.CustomUserDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("password");
        adminUser.setRole(UserRole.ADMIN);

        User memberUser = new User();
        memberUser.setId(2L);
        memberUser.setEmail("member@test.com");
        memberUser.setPassword("password");
        memberUser.setRole(UserRole.MEMBER);

        CustomUserDetails adminDetails = new CustomUserDetails(
                adminUser,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        CustomUserDetails memberDetails = new CustomUserDetails(
                memberUser,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        return new InMemoryUserDetailsManager(adminDetails, memberDetails);
    }
}