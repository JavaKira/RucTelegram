package com.github.javakira.statistic;

import com.github.javakira.context.ChatContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;

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

    public void addCommandUsage(String command, Chat chat, ChatContext context) {
        save(Statistic.builder()
                .data(command)
                .setContext(context)
                .setChat(chat)
                .type(Statistic.commandUsageType)
                .date(LocalDateTime.now())
                .build()
        );
    }
}
