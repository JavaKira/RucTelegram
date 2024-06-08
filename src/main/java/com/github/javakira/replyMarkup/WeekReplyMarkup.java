package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class WeekReplyMarkup extends InlineKeyboardMarkup {
    public final boolean isScheduleShorted;

    public WeekReplyMarkup(boolean isScheduleShorted) {
        this.isScheduleShorted = isScheduleShorted;
        init(LocalDate.now());
    }

    public WeekReplyMarkup(boolean isScheduleShorted, LocalDate date) {
        this.isScheduleShorted = isScheduleShorted;
        init(date);
    }

    private WeekReplyMarkup(boolean isScheduleShorted, @NonNull List<List<InlineKeyboardButton>> keyboard) {
        super(keyboard);
        this.isScheduleShorted = isScheduleShorted;
    }

    private void init(LocalDate now) {
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<List<InlineKeyboardButton>> keyboard = new java.util.ArrayList<>(List.of(
                List.of(InlineKeyboardButton.builder().text(weekStart.minusWeeks(1) + " - " + weekEnd.minusWeeks(1)).callbackData("week " + now.minusWeeks(1)).build(),
                        InlineKeyboardButton.builder().text(weekStart.plusWeeks(1) + " - " + weekEnd.plusWeeks(1)).callbackData("week " + now.plusWeeks(1)).build())
                ));

        if (isScheduleShorted)
            keyboard.add(List.of(InlineKeyboardButton.builder().text("Выводить полное расписание").callbackData("weekScheduleShorted_false " + now).build()));
        else
            keyboard.add(List.of(InlineKeyboardButton.builder().text("Выводить сокращенное расписание").callbackData("weekScheduleShorted_true " + now).build()));

        setKeyboard(keyboard);

    }
}
