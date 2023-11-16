package com.github.javakira.context;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    /**Id of telegram user*/
    @Id
    private long id;
    /**Optional. id of chat-bot with user*/
    private Long chatId;
    private LocalDateTime creationDate;
    private String firstName, lastName, username;
    private boolean isPremium;
}
