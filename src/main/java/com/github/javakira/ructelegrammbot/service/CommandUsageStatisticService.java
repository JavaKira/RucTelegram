package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.statistic.CommandUsageStatistic;
import com.github.javakira.ructelegrammbot.repository.CommandUsageStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandUsageStatisticService {
    public CommandUsageStatisticRepository repository;
    @Autowired
    public CommandUsageStatisticService(CommandUsageStatisticRepository repository) {
        this.repository = repository;
    }

    public void add(CommandUsageStatistic statistic) {
        repository.save(statistic);
    }
}
