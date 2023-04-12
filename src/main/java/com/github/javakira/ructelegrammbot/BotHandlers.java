package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.handler.BotHandler;
import jakarta.validation.constraints.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class BotHandlers {
    private final Map<Class<? extends BotHandler>, BotHandler> handlers = new HashMap<>();

    public void add(Class<? extends BotHandler> type, BotHandler handler) {
        handlers.put(type, handler);
    }

    public <T extends BotHandler> T get(Class<T> type) {
        return (T) handlers.get(type);
    }

    public void onUpdateReceived(@NotNull Update update) {
        handlers.values().forEach(handler -> handler.onUpdateReceived(update));
    }
}
