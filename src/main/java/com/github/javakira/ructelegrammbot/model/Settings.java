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

    //Chat data
    private String username;
    private String lastName;
    private String firstName;
    private String title;

    private boolean isEmployee;

    private String branch;
    private String branchTitle;

    private String employeeKey;
    private String employeeTitle;

    private String kit;
    private String kitTitle;
    private String groupKey;
    private String groupTitle;

    public Settings(long chatId) {
        this.chatId = chatId;
    }
}
