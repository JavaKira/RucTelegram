package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.config.BotConfig;
import com.github.javakira.ructelegrammbot.model.*;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import com.github.javakira.ructelegrammbot.service.SettingsService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
    final BotConfig config;

    @Autowired
    private SettingsService service;

    private final Map<String, Consumer<Update>> commands = new HashMap<>();

    public Bot(BotConfig config) {
        this.config = config;
        registerCommands();
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

            if (commands.containsKey(split[0].replace("@RucSchedule_bot", "")))
                commands.get(split[0].replace("@RucSchedule_bot", "")).accept(update);
        }

        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            String[] split = data.split(" ");

            switch (split[0]) {
                case "branch":
                    setBranch(chatId, split[1]);
                    sendKits(chatId);
                    break;
                case "kit":
                    setKit(chatId, split[1]);
                    sendGroups(chatId);
                    break;
                case "group": //todo add sendSettings method that shows all settings
                    setGroup(chatId, split[1]);
                    sendSettings(chatId);
                    break;
                case "groupnext":
                    EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                    editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessageReplyMarkup.setChatId(chatId);
                    createGroupsKeyboard(chatId, Integer.parseInt(split[1]) + 1).thenAccept(inlineKeyboardMarkup -> {
                        editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            execute(editMessageReplyMarkup);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return;
                case "groupprev":
                    EditMessageReplyMarkup editMessageReplyMarkup1 = new EditMessageReplyMarkup();
                    editMessageReplyMarkup1.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                    editMessageReplyMarkup1.setChatId(chatId);
                    createGroupsKeyboard(chatId, Integer.parseInt(split[1]) - 1).thenAccept(inlineKeyboardMarkup -> {
                        editMessageReplyMarkup1.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            execute(editMessageReplyMarkup1);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return;
            }

            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setChatId(chatId);
            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void registerCommands() {
        Consumer<Update> today = update -> {
            long chatId = update.getMessage().getChatId();
            scheduleToday(chatId);
        };

        Consumer<Update> tomorrow = update -> {
            long chatId = update.getMessage().getChatId();
            scheduleToday(chatId);
        };

        Consumer<Update> setup = update -> {
            long chatId = update.getMessage().getChatId();
            sendBranches(chatId, update.getMessage());
        };

        registerCommand("/start", update -> {
            long chatId = update.getMessage().getChatId();
            String memberName = update.getMessage().getFrom().getFirstName();
            startBot(chatId, memberName);
        });

        //todo можно наверно сделать через String... вариации команды
        registerCommand("/настроить", setup);
        registerCommand("/setup", setup);

        registerCommand("/сегодня", today);
        registerCommand("/today", today);

        registerCommand("/завтра", tomorrow);
        registerCommand("/tomorrow", tomorrow);
    }

    private void registerCommand(String command, Consumer<Update> action) {
        commands.put(command, action);
    }

    private void sendBranches(long chatId, Message message) {
        if (!service.isSettingsExist4Chat(chatId))
            service.createSettings(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери филиал\n");
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        scheduleParser.getBranches().thenAccept(branches -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            for (Branch branch : branches) {
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text(branch.title())
                        .callbackData("branch " + branch.value())
                        .build())
                );
            }

            sendMessage.setReplyToMessageId(message.getMessageId());
            sendMessage.setReplyMarkup(new InlineKeyboardMarkup(buttons));

            try {
                execute(sendMessage);
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
        });
    }

    private void sendKits(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери набор\n");
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        scheduleParser.getKits(service.getSettings(chatId).getBranch()).thenAccept(kits -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            for (Kit kit : kits) {
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text(kit.title())
                        .callbackData("kit " + kit.value())
                        .build())
                );
            }

            sendMessage.setReplyMarkup(new InlineKeyboardMarkup(buttons));
            try {
                execute(sendMessage);
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
        });
    }

    private void sendGroups(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери группу\n");
        createGroupsKeyboard(chatId, 0).thenAccept(inlineKeyboardMarkup -> {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
        });
    }

    private CompletableFuture<InlineKeyboardMarkup> createGroupsKeyboard(long chatId, int page) {
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        return scheduleParser.getGroups(
                service.getSettings(chatId).getBranch(),
                service.getSettings(chatId).getKit()
        ).thenApply(groups -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            List<InlineKeyboardButton> buttons1 = new LinkedList<>();
            if (page != 0)
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("предыдущее")
                                .callbackData("groupprev " + page)
                                .build()
                );
            for (int i = page * 14; i < page * 14 + 14; i++) {
                Group group = groups.get(i);
                buttons1.add(InlineKeyboardButton.builder()
                        .text(group.title())
                        .callbackData("group " + group.value())
                        .build());

                if (buttons1.size() >= 2) {
                    buttons.add(buttons1);
                    buttons1 = new LinkedList<>();
                }
            }
            if ((page * 14 + 14) < groups.size()) {
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("следующее")
                                .callbackData("groupnext " + page)
                                .build()
                );
                buttons.add(buttons1);
            }

            return new InlineKeyboardMarkup(buttons);
        });
    }

    private void sendSettings(long chatId) {
        String stringBuilder = "Настройки этого чата:"
                + "\n" + "Филиал: " + service.getSettings(chatId).getBranch()
                + "\n" + "Набор: " + service.getSettings(chatId).getKit()
                + "\n" + "Группа: " + service.getSettings(chatId).getGroupKey();
        sendString(chatId, stringBuilder);
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