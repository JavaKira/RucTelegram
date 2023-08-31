package com.github.javakira;

import com.github.javakira.command.CommandService;
import com.github.javakira.config.BotConfig;
import com.github.javakira.context.ChatContextService;
import com.github.javakira.parser.ParserService;
import com.github.javakira.reply.ReplyExecutorService;
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
    public final ParserService parserService;
    public final ReplyExecutorService replyExecutorService;
    public final ChatContextService chatContextService;

    public Bot(
            BotConfig config,
            @Autowired CommandService commandService,
            @Autowired ParserService parserService,
            @Autowired ReplyExecutorService replyExecutorService,
            @Autowired ChatContextService chatContextService
    ) {
        this.config = config;
        this.commandService = commandService;
        this.parserService = parserService;
        this.replyExecutorService = replyExecutorService;
        this.chatContextService = chatContextService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        commandService.onUpdateReceived(this, update);
        replyExecutorService.onUpdateReceived(this, update);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
