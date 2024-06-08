package com.github.javakira;

import com.github.javakira.api.ScheduleApiService;
import com.github.javakira.callback.CallbackService;
import com.github.javakira.command.CommandService;
import com.github.javakira.config.BotConfig;
import com.github.javakira.context.ChatContextService;
import com.github.javakira.context.UserContextService;
import com.github.javakira.reply.ReplyExecutorService;
import com.github.javakira.statistic.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    private final BotConfig config;

    public final CommandService commandService;
    public final CallbackService callbackService;
    public final ScheduleApiService parserService;
    public final ReplyExecutorService replyExecutorService;
    public final ChatContextService chatContextService;
    public final UserContextService userContextService;
    public final StatisticService statisticService;

    @Override
    public void onUpdateReceived(Update update) {
        commandService.onUpdateReceived(this, update);
        replyExecutorService.onUpdateReceived(this, update);
        callbackService.onUpdateReceived(this, update);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
