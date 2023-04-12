package com.github.javakira.ructelegrammbot.handler;

import com.github.javakira.ructelegrammbot.Bot;
import com.github.javakira.ructelegrammbot.model.Group;
import com.github.javakira.ructelegrammbot.model.Kit;
import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import com.github.javakira.ructelegrammbot.service.SettingsService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class CallbackQueryHandler implements BotHandler {
    private final Bot bot;
    private final SettingsService service;

    public CallbackQueryHandler(Bot bot, SettingsService service) {
        this.bot = bot;
        this.service = service;
    }

    @Override
    public void onUpdateReceived(Update update) {
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
                            bot.execute(editMessageReplyMarkup);
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
                            bot.execute(editMessageReplyMarkup1);
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
                bot.execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
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
                bot.execute(sendMessage);
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
                bot.execute(sendMessage);
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
        });
    }

    private void sendSettings(long chatId) {
        String stringBuilder = "Настройки этого чата:"
                + "\n" + "Филиал: " + service.getSettings(chatId).getBranch()
                + "\n" + "Набор: " + service.getSettings(chatId).getKit()
                + "\n" + "Группа: " + service.getSettings(chatId).getGroupKey();
        sendString(chatId, stringBuilder);
    }

    private void sendString(long chatId, String string) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(string);
        try {
            bot.execute(message);
        } catch (TelegramApiException e){
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
