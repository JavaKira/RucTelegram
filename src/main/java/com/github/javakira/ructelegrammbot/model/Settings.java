package com.github.javakira.ructelegrammbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "tg_data")
public class Settings {
    @Id
    @GeneratedValue
    private long id;
    private long chatId;

    private boolean isEmployee;

    private String branch;

    private String employeeKey;

    private String kit;
    private String groupKey;

    public Settings(long chatId) {
        this.chatId = chatId;
    }
}
