package com.github.javakira.command;

import com.github.javakira.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Command {
    String getUsage();
    void execute(Bot bot, Update update);
}
