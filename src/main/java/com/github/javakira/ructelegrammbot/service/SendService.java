package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.*;
import com.github.javakira.ructelegrammbot.parser.*;
import com.github.javakira.ructelegrammbot.statistic.ExceptionStatistic;
import com.github.javakira.ructelegrammbot.statistic.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class SendService {
    private StatisticService statisticService;
    private ParserService parserService;

    @Autowired
    public SendService(StatisticService statisticService, ParserService parserService) {
        this.statisticService = statisticService;
        this.parserService = parserService;
    }

    public CompletableFuture<SendMessage> scheduleToday(long chatId, Settings settings) {
        AtomicReference<SendMessage> returnValue = new AtomicReference<>();
        return schedule(chatId, settings, new Date()).thenApply(listScheduleParserResult -> {
            try {
                return listScheduleParserResult.get();
            } catch (ScheduleParserException e) {
                return sendException(chatId, e);
            }
        }).thenApply(object -> {
            if (object instanceof SendMessage)
                return (SendMessage) object;
            else {
                Cards cards = (Cards) object;
                Optional<Card> card = cards.getToday();
                if (card.isPresent()) {
                    returnValue.set(sendCard(chatId, card.get(), settings));
                } else {
                    returnValue.set(sendString(chatId, "На сегодня расписания для " + (settings.isEmployee() ? settings.getEmployeeTitle() : settings.getGroupTitle()) + " нет."));
                }

                return returnValue.get();
            }
        });
    }

    public CompletableFuture<SendMessage> scheduleTomorrow(long chatId, Settings settings) {
        AtomicReference<SendMessage> returnValue = new AtomicReference<>();
        Date date = new Date();
        date.setDate(date.getDate() + 1);
        return schedule(chatId, settings, date).thenApply(listScheduleParserResult -> {
            try {
                return listScheduleParserResult.get();
            } catch (ScheduleParserException e) {
                return sendException(chatId, e);
            }
        }).thenApply(object -> {
            if (object instanceof SendMessage)
                return (SendMessage) object;
            else {
                Cards cards = (Cards) object;
                Optional<Card> card = cards.getTomorrow();
                if (card.isPresent()) {
                    returnValue.set(sendCard(chatId, card.get(), settings));
                } else {
                    Calendar calendar = new GregorianCalendar();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("На завтра расписания для ").append(settings.isEmployee() ? settings.getEmployeeTitle() : settings.getGroupTitle()).append(" нет.");
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
                        stringBuilder.append("\\n\\nПохоже на то, что вы смотрите расписание на понедельник. Оно обычно появляется только в понедельник в 0:00");

                    returnValue.set(sendString(chatId, stringBuilder.toString()));
                }

                return returnValue.get();
            }
        });
    }

    private CompletableFuture<ScheduleParserResult<Cards>> schedule(long chatId, Settings settings, Date date) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        CompletableFuture<ScheduleParserResult<Cards>> future;
        if (settings.isEmployee())
            future = parserService.getEmployeeCards(settings.getBranch(), settings.getEmployeeKey());
        else
            future = parserService.getGroupCards(settings.getBranch(), settings.getKit(), settings.getGroupKey(), date);

        return future;
    }

    public CompletableFuture<SendMessage> sendSchedule(long chatId, Settings settings, Date date) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        CompletableFuture<ScheduleParserResult<Cards>> future;
        if (settings.isEmployee())
            future = parserService.getEmployeeCards(settings.getBranch(), settings.getEmployeeKey());
        else
            future = parserService.getGroupCards(settings.getBranch(), settings.getKit(), settings.getGroupKey(), date);

        return future.thenApply(result -> {
            try {
                Cards cards = result.get();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(formatCardHeader(cards.getList().get(0).date(), settings));
                for (int i = 0; i < cards.getList().size(); i++) {
                    stringBuilder.append(formatCard(cards.getList().get(i)));
                    if (i + 1 < cards.getList().size()) {
                        stringBuilder.append("\n");
                        stringBuilder.append(formatShortCardHeader(cards.getList().get(i + 1).date()));
                        stringBuilder.append("\n");
                    }
                }

                SendMessage sendMessage = sendString(chatId, stringBuilder.toString());
                //todo Избавиться от Date в пользу LocalDate
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                LocalDate now = LocalDate.of(1900 + date.getYear(), date.getMonth(), date.getDate());
                LocalDate weekStart = now.with(DayOfWeek.MONDAY).plusMonths(1);
                LocalDate weekEnd = weekStart.plusDays(6);
                inlineKeyboardMarkup.setKeyboard(List.of(
                        List.of(InlineKeyboardButton.builder().text(weekStart.minusWeeks(1) + " - " + weekEnd.minusWeeks(1)).callbackData("schedule " + now.minusWeeks(1)).build(),
                                InlineKeyboardButton.builder().text(weekStart.plusWeeks(1) + " - " + weekEnd.plusWeeks(1)).callbackData("schedule " + now.plusWeeks(1)).build())
                ));
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                return sendMessage;
            } catch (ScheduleParserException e) {
                return sendException(chatId, e);
            }
        });
    }

    public String formatCard(Card card) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair pair : card.pairList()) {
            stringBuilder.append("\n");
            stringBuilder.append(pair.getIndex() + 1).append(". ").append(pair.getName()).append(" ").append("\n");
            stringBuilder.append(pair.getBy()).append("\n");
            stringBuilder.append(pair.getPlace()).append("\n");
            stringBuilder.append(pair.getType()).append("\n");
        }

        return stringBuilder.toString();
    }

    public String formatShortCardHeader(Date date) {
        return date.getDate() +
                "." +
                (date.getMonth() + 1) +
                "." +
                (date.getYear() + 1900) +
                " (" + formatDayOfWeek(date) + ")";
    }

    public String formatCardHeader(Date date, Settings settings) {
        return "#Расписание " +
                (settings.isEmployee() ? settings.getEmployeeTitle() : settings.getGroupTitle()) +
                " на " +
                date.getDate() +
                "." +
                (date.getMonth() + 1) +
                "." +
                (date.getYear() + 1900) +
                " (" + formatDayOfWeek(date) + ")";
    }

    public SendMessage sendCard(long chatId, Card card, Settings settings) {
        return sendString(chatId,formatCardHeader(card.date(), settings) + "\n" + formatCard(card));
    }

    private String formatDayOfWeek(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY -> "воскресенье";
            case Calendar.MONDAY -> "Понедельник";
            case Calendar.TUESDAY -> "Вторник";
            case Calendar.WEDNESDAY -> "Среда";
            case Calendar.THURSDAY -> "Четверг";
            case Calendar.FRIDAY -> "Пятница";
            case Calendar.SATURDAY -> "Суббота";
            default -> "Хер знает че за день недели";
        };
    }

    public CompletableFuture<SendMessage> sendKits(long chatId, Settings settings) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери набор\n");
        return parserService.getKits(settings.getBranch()).thenApply(result -> {
            try {
                List<Kit> kits = result.get();
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
            } catch (Exception e) {
                return sendException(chatId, e);
            }
        });
    }

    public SendMessage sendIsEmployeeChoose(long chatId, Message message) {
        SendMessage sendMessage = sendString(chatId, "Кем будет использоваться бот?");
        sendMessage.setReplyToMessageId(message.getMessageId());
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                List.of(List.of(
                        InlineKeyboardButton.builder().text("Преподаватель").callbackData("employeeTrue").build(),
                        InlineKeyboardButton.builder().text("Студент").callbackData("studentTrue").build()
                ))
        );
        sendMessage.setReplyMarkup(inlineKeyboard);
        return sendMessage;
    }

    public CompletableFuture<SendMessage> sendBranches(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери филиал\n");
        return parserService.getBranches().thenApply(listScheduleParserResult -> {
            try {
                return listScheduleParserResult.get();
            } catch (ScheduleParserException e) {
                return sendException(chatId, e);
            }
        }).thenApply(object -> {
            if (object instanceof SendMessage)
                return (SendMessage) object;
            else {
                List<Branch> branches = (List<Branch>) object;
                List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
                for (Branch branch : branches) {
                    buttons.add(List.of(InlineKeyboardButton.builder()
                            .text(branch.title())
                            .callbackData("branch " + branch.value())
                            .build())
                    );
                }

                sendMessage.setReplyMarkup(new InlineKeyboardMarkup(buttons));
                return sendMessage;
            }
        });
    }

    public SendMessage sendEmployee(long chatId, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери сотрудника\n");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }

    public SendMessage sendPairSchedule(long chatId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Расписание пар:\n\n");
        for (int i = 0; i < 7; i++) {
            stringBuilder.append(i + 1).append(" пара - ").append(Pair.getTimeByIndex(i)).append("\n");
        }

        return sendString(chatId, stringBuilder.toString());
    }

    public SendMessage sendWarning(long chatId, Message message) {
        SendMessage sendMessage = sendString(chatId, "Данный бот не гарантирует точного расписания\n\nИсточник всех данных - официальный сайт (schedule.ruc.su)\n\nАвтор сего чуда - @javapedik\nБольше информации - vk.com/javapedik?w=wall589441434_639");
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
        if (settings.isEmployee())
            return sendString(chatId, "Настройки этого чата:"
                + "\n" + "Филиал: " + settings.getBranchTitle()
                + "\n" + "Работник: " + settings.getEmployeeTitle());
        else
            return sendString(chatId, "Настройки этого чата:"
                    + "\n" + "Филиал: " + settings.getBranchTitle()
                    + "\n" + "Набор: " + settings.getKitTitle()
                    + "\n" + "Группа: " + settings.getGroupTitle());
    }

    public SendMessage sendNotConfigured(long chatId) {
        return sendString(chatId, "Бот не настроен. Используй /setup@RucSchedule_bot");
    }

    public SendMessage sendServerNotResponding(long chatId) {
        return sendString(chatId, "Сервер не отвечает");
    }

    public SendMessage sendException(long chatId, Exception e) {
        StringBuilder statisticStackTraceBuilder = new StringBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < 1; i++) {
            StackTraceElement str = stackTrace[i];
            statisticStackTraceBuilder.append(str.toString()).append("\n");
        }

        statisticService.add(ExceptionStatistic.builder()
                .head(e.toString())
                .stacktrace(statisticStackTraceBuilder.toString())
                .date(new Date())
                .build());

        if (e instanceof ServerNotRespondingException)
            return sendServerNotResponding(chatId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Ошибка во время исполнения:\n\n");
        stringBuilder.append(e.toString()).append("\n\n");
        for (StackTraceElement str : e.getStackTrace())
            stringBuilder.append(str.toString()).append("\n");

        stringBuilder.append("\n\n@Javapedik уже скорее всего получил пинка под зад, но вы можете добавить ещё, чтобы он ускорился");
        return sendString(chatId,
                stringBuilder.toString());
    }

    public SendMessage sendStart(long chatId) {
        return sendString(chatId, "Чтобы начать, настрой бота — /setup@RucSchedule_bot");
    }

    public SendMessage sendString(long chatId, String string) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(string);
        return message;
    }
}
