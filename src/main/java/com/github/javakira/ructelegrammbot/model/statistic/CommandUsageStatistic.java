package com.github.javakira.ructelegrammbot.model.statistic;

import com.github.javakira.ructelegrammbot.model.Settings;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "tg_stat_command")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandUsageStatistic {
    @Id
    @GeneratedValue
    public long id;

    public String command;
    public Date date;

    //Chat data //ToDo можно вынести 4 нижних поля в 1 класс
    public String chatUsername;
    public String chatLastName;
    public String chatFirstName;
    public String chatTitle;
    @OneToOne
    public Settings settings;

    /**
     *username of user what use command
     */
    public String userUsername;
}
