package com.github.javakira.ructelegrammbot.statistic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {
    public CommandUsageStatisticRepository commandRepository;
    public CallbackUsageStatisticRepository callbackRepository;
    public ExceptionStatisticRepository exceptionRepository;

    @Autowired
    public StatisticService(CommandUsageStatisticRepository commandRepository, CallbackUsageStatisticRepository callbackRepository, ExceptionStatisticRepository exceptionRepository) {
        this.commandRepository = commandRepository;
        this.callbackRepository = callbackRepository;
        this.exceptionRepository = exceptionRepository;
    }

    public void add(CommandUsageStatistic statistic) {
        commandRepository.save(statistic);
    }

    public void add(CallbackUsageStatistic statistic) {
        callbackRepository.save(statistic);
    }

    public void add(ExceptionStatistic statistic) {
        exceptionRepository.save(statistic);
    }
}
