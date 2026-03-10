package com.amalitech.communityboard.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "categories")
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @NotBlank(message = "name cannot be empty")
    @Column(unique = true)
    @Size(min = 2,message = "name cannot be less than 2 letters")
    private String name;

    @Column(nullable = true)
    private String description;

    @UpdateTimestamp
    private LocalDateTime createdAt;


    public Category() {}
}
