package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class WeekReplyMarkup extends InlineKeyboardMarkup {
    public WeekReplyMarkup() {
        init(LocalDate.now());
    }

    public WeekReplyMarkup(LocalDate date) {
        init(date);
    }

    private WeekReplyMarkup(@NonNull List<List<InlineKeyboardButton>> keyboard) {
        super(keyboard);
    }

    private void init(LocalDate now) {
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        setKeyboard(List.of(
                List.of(InlineKeyboardButton.builder().text(weekStart.minusWeeks(1) + " - " + weekEnd.minusWeeks(1)).callbackData("week " + now.minusWeeks(1)).build(),
                        InlineKeyboardButton.builder().text(weekStart.plusWeeks(1) + " - " + weekEnd.plusWeeks(1)).callbackData("week " + now.plusWeeks(1)).build())
        ));

    }
}
