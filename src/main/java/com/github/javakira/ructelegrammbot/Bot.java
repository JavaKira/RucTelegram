package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.config.BotConfig;
import com.github.javakira.ructelegrammbot.model.Card;
import com.github.javakira.ructelegrammbot.model.Cards;
import com.github.javakira.ructelegrammbot.model.Pair;
import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import com.github.javakira.ructelegrammbot.service.SettingsService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
                case "/сегодня":
                case "/today":
                    scheduleToday(chatId);
                    break;
                case "/завтра":
                case "/tomorrow":
                    scheduleTomorrow(chatId);
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

    private void scheduleToday(long chatId) {
        schedule(chatId, cards -> {
            Optional<Card> card = cards.getToday();
            if (card.isPresent()) {
                sendCard(chatId, card.get());
            } else {
                sendString(chatId, "На сегодня расписания нет.");
            }
        });
    }

    private void scheduleTomorrow(long chatId) {
        schedule(chatId, cards -> {
            Optional<Card> card = cards.getTomorrow();
            if (card.isPresent()) {
                sendCard(chatId, card.get());
            } else {
                sendString(chatId, "На завтра расписания нет.");
            }
        });
    }

    private void schedule(long chatId, Consumer<Cards> cardsConsumer) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (!service.isSettingsExist4Chat(chatId)) {
            sendNotConfigured(chatId);
        } else {
            Settings settings = service.getSettings(chatId);
            ScheduleParser parser = new HtmlScheduleParser();
            CompletableFuture<Cards> future;
            if (settings.getBranch() == null) {
                sendNotConfigured(chatId);
                return;
            }

            if (settings.isEmployee()) {
                if (settings.getEmployeeKey() == null) {
                    sendNotConfigured(chatId);
                    return;
                }

                future = parser.getEmployeeCards(settings.getBranch(), settings.getEmployeeKey());
            } else {
                if (settings.getKit() == null) {
                    sendNotConfigured(chatId);
                    return;
                }

                if (settings.getGroupKey() == null) {
                    sendNotConfigured(chatId);
                    return;
                }

                future = parser.getGroupCards(settings.getBranch(), settings.getKit(), settings.getGroupKey());
            }

            future.thenAccept(cardsConsumer);
        }

    }

    private void sendCard(long chatId, Card card) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Расписание на ")
                .append(card.date().getDate())
                .append(".")
                .append(card.date().getMonth() + 1)
                .append(".")
                .append(card.date().getYear() + 1900)
                .append("\n");
        for (Pair pair : card.pairList()) {
            stringBuilder.append("\n");
            stringBuilder.append(pair.getIndex() + 1).append(". ").append(pair.getName()).append("\n");
            stringBuilder.append(pair.getBy()).append("\n");
            stringBuilder.append(pair.getPlace()).append("\n");
            stringBuilder.append(pair.getType()).append("\n");
        }

        message.setText(stringBuilder.toString());

        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void sendNotConfigured(long chatId) {
        sendString(chatId, "Бот не настроен");
    }

    private void sendString(long chatId, String string) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(string);
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }
}