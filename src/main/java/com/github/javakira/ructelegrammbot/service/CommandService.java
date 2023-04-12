package com.github.javakira.ructelegrammbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class CommandService implements BotService {
    private final Map<String, Consumer<Update>> commands = new HashMap<>();

    public void putCommand(String command, Consumer<Update> action) {
        commands.put(command, action);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String[] split = messageText.split(" ");

            if (commands.containsKey(split[0].replace("@RucSchedule_bot", "")))
                commands.get(split[0].replace("@RucSchedule_bot", "")).accept(update);
        }
    }
}
