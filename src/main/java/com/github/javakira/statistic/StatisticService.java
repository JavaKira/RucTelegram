package com.github.javakira.statistic;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatisticService {
    private final StatisticRepository repository;


    public StatisticService(StatisticRepository repository) {
        this.repository = repository;
    }

    public void save(Statistic statistic) {
        repository.save(statistic);
    }

    public void addCommandUsage(String command) {
        save(Statistic.builder()
                .data(command)
                .type(Statistic.commandUsageType)
                .date(LocalDateTime.now())
                .build()
        );
    }
}
