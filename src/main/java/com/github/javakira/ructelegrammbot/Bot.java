package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.config.BotConfig;
import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.service.SettingsService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
    final BotConfig config;

    @Autowired
    private SettingsService service;

    public Bot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String[] split = messageText.split(" ");
            long chatId = update.getMessage().getChatId();
            String memberName = update.getMessage().getFrom().getFirstName();

            switch (split[0]) {
                case "/start":
                    startBot(chatId, memberName);
                    break;
                case "/branch":
                    setBranch(chatId, messageText.split(" ")[1]);
                    break;
                case "/employee":
                    setEmployee(chatId, messageText.split(" ")[1]);
                    break;
                case "/kit":
                    setKit(chatId, messageText.split(" ")[1]);
                    break;
                case "/group":
                    setGroup(chatId, messageText.split(" ")[1]);
                    break;
                default:
                    log.info("Unexpected message");
            }
        }
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (!service.isSettingsExist4Chat(chatId)) {
            service.createSettings(chatId);
            message.setText("Привет, " + userName + "! Я бот с расписанием РУКа. Используй /help для помощи.");
        } else
            message.setText("Используй /help для помощи.");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void setBranch(long chatId, String argument) {
        Settings settings = service.getSettings(chatId);
        settings.setBranch(argument);
        service.saveSettings(settings);
    }

    private void setEmployee(long chatId, String argument) {
        Settings settings = service.getSettings(chatId);
        settings.setEmployeeKey(argument);
        service.saveSettings(settings);
    }

    private void setKit(long chatId, String argument) {
        Settings settings = service.getSettings(chatId);
        settings.setKit(argument);
        service.saveSettings(settings);
    }

    private void setGroup(long chatId, String argument) {
        Settings settings = service.getSettings(chatId);
        settings.setGroupKey(argument);
        service.saveSettings(settings);
    }
}