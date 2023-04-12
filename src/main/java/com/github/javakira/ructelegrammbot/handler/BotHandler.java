package com.github.javakira.ructelegrammbot.handler;

import jakarta.validation.constraints.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotHandler {
    void onUpdateReceived(@NotNull Update update);
}
