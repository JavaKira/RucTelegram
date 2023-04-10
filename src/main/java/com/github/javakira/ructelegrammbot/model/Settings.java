package com.github.javakira.ructelegrammbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "tg_data")
public class Settings {
    @Id
    private long id;
    private long chatId;

    private String branch;

    private String employee;

    private String kit;
    private String groupKey;
}
