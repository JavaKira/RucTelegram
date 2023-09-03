package com.github.javakira;

import com.github.javakira.callback.CallbackService;
import com.github.javakira.command.CommandService;
import com.github.javakira.config.BotConfig;
import com.github.javakira.context.ChatContextService;
import com.github.javakira.parser.ParserService;
import com.github.javakira.reply.ReplyExecutorService;
import com.github.javakira.statistic.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
    final BotConfig config;

    public final CommandService commandService;
    public final CallbackService callbackService;
    public final ParserService parserService;
    public final ReplyExecutorService replyExecutorService;
    public final ChatContextService chatContextService;
    public final StatisticService statisticService;

    public Bot(
            BotConfig config,
            @Autowired CallbackService callbackService,
            @Autowired CommandService commandService,
            @Autowired ParserService parserService,
            @Autowired ReplyExecutorService replyExecutorService,
            @Autowired ChatContextService chatContextService,
            @Autowired StatisticService statisticService
    ) {
        this.config = config;
        this.statisticService = statisticService;
        this.callbackService = callbackService;
        this.commandService = commandService;
        this.parserService = parserService;
        this.replyExecutorService = replyExecutorService;
        this.chatContextService = chatContextService;
    }

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
