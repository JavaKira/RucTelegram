package com.github.javakira.ructelegrammbot.service;

import jakarta.validation.constraints.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {
    void onUpdateReceived(@NotNull Update update);
}
