package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.*;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class SendService {
    public CompletableFuture<SendMessage> scheduleToday(long chatId, Settings settings) {
        AtomicReference<SendMessage> returnValue = new AtomicReference<>();
        return schedule(chatId, settings).thenApply(cards -> {
            Optional<Card> card = cards.getToday();
            if (card.isPresent()) {
                returnValue.set(sendCard(chatId, card.get(), settings));
            } else {
                returnValue.set(sendString(chatId, "На сегодня расписания нет."));
            }

            return returnValue.get();
        });
    }

    public CompletableFuture<SendMessage> scheduleTomorrow(long chatId, Settings settings) {
        AtomicReference<SendMessage> returnValue = new AtomicReference<>();
        return schedule(chatId, settings).thenApply(cards -> {
            Optional<Card> card = cards.getTomorrow();
            if (card.isPresent()) {
                returnValue.set(sendCard(chatId, card.get(), settings));
            } else {
                returnValue.set(sendString(chatId, "На завтра расписания нет."));
            }

            return returnValue.get();
        });
    }

    private CompletableFuture<Cards> schedule(long chatId, Settings settings) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        ScheduleParser parser = new HtmlScheduleParser();
        CompletableFuture<Cards> future;
        if (settings.isEmployee())
            future = parser.getEmployeeCards(settings.getBranch(), settings.getEmployeeKey());
        else
            future = parser.getGroupCards(settings.getBranch(), settings.getKit(), settings.getGroupKey());

        return future;
    }

    public SendMessage sendCard(long chatId, Card card, Settings settings) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("#Расписание ").append(settings.getGroupTitle()).append(" на ")
                .append(card.date().getDate())
                .append(".")
                .append(card.date().getMonth() + 1)
                .append(".")
                .append(card.date().getYear() + 1900)
                .append(" (").append(formatDayOfWeek(card.date())).append(")")
                .append("\n");
        for (Pair pair : card.pairList()) {
            stringBuilder.append("\n");
            stringBuilder.append(pair.getIndex() + 1).append(". ").append(pair.getName()).append("\n");
            stringBuilder.append(pair.getBy()).append("\n");
            stringBuilder.append(pair.getPlace()).append("\n");
            stringBuilder.append(pair.getType()).append("\n");
        }

        message.setText(stringBuilder.toString());
        return message;
    }

    private String formatDayOfWeek(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY -> {
                return "воскресенье";
            }
            case Calendar.MONDAY -> {
                return "Понедельник";
            }
            case Calendar.TUESDAY -> {
                return "Вторник";
            }
            case Calendar.WEDNESDAY -> {
                return "Среда";
            }
            case Calendar.THURSDAY -> {
                return "Четверг";
            }
            case Calendar.FRIDAY -> {
                return "Пятница";
            }
            case Calendar.SATURDAY -> {
                return "Суббота";
            }
            default -> {
                return "Хер знает че за день недели";
            }
        }
    }

    public CompletableFuture<SendMessage> sendKits(long chatId, Settings settings) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери набор\n");
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        return scheduleParser.getKits(settings.getBranch()).thenApply(kits -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            for (Kit kit : kits) {
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text(kit.title())
                        .callbackData("kit " + kit.value())
                        .build())
                );
            }

            sendMessage.setReplyMarkup(new InlineKeyboardMarkup(buttons));
            return sendMessage;
        });
    }

    public CompletableFuture<SendMessage> sendBranches(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери филиал\n");
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        return scheduleParser.getBranches().thenApply(branches -> {
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
            return sendMessage;
        });
    }

    public SendMessage sendWarning(long chatId, Message message) {
        SendMessage sendMessage = sendString(chatId, "Данный бот не гарантирует точного расписания\n\nИсточник всех данных - официальный сайт (schedule.ruc.su)");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(List.of(
               InlineKeyboardButton.builder().text("Я понимаю").callbackData("warning " + message.getMessageId()).build()
        )));
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public SendMessage sendGroups(long chatId, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери группу\n");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }

    public SendMessage sendSettings(long chatId, Settings settings) {
        String stringBuilder = "Настройки этого чата:"
                + "\n" + "Филиал: " + settings.getBranchTitle()
                + "\n" + "Набор: " + settings.getKitTitle()
                + "\n" + "Группа: " + settings.getGroupTitle();
        return sendString(chatId, stringBuilder);
    }

    public SendMessage sendNotConfigured(long chatId) {
        return sendString(chatId, "Бот не настроен");
    }

    public SendMessage sendString(long chatId, String string) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(string);
        return message;
    }
}
