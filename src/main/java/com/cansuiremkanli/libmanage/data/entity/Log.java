package com.cansuiremkanli.libmanage.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs")
@Data
public class Log {

    @Id
    @GeneratedValue
    private UUID id;

    private String logLevel;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    private String path;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
