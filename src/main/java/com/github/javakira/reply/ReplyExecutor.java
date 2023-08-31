package com.github.javakira.reply;

import com.github.javakira.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ReplyExecutor {
    void execute(Bot bot, Update update);
}
