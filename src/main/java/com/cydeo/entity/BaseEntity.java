package com.cydeo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime insertDateTime;
    @Column(updatable = false, nullable = false)
    private Long insertUserId;
    @Column(nullable = false)
    private LocalDateTime lastUpdateDateTime;
    @Column(nullable = false)
    private Long lastUpdateUserId;

    private Boolean isDeleted = false;

    @PrePersist
    private void onPrePersist(){
        insertDateTime = LocalDateTime.now();
        lastUpdateDateTime = LocalDateTime.now();
        insertUserId = 1L;
        lastUpdateUserId = 1L;
    }

    @PreUpdate
    private void onPreUpdate(){
        lastUpdateDateTime = LocalDateTime.now();
        lastUpdateUserId = 1L;
    }

}
