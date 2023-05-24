package com.github.javakira.ructelegrammbot.statistic;

import com.github.javakira.ructelegrammbot.model.Settings;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "tg_stat_exception")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionStatistic {
    @Id
    @GeneratedValue
    public long id;

    public String head;
    public String stacktrace;
    public Date date;
}
