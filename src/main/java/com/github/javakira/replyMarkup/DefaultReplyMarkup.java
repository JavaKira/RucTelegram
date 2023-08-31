package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class DefaultReplyMarkup extends ReplyKeyboardMarkup {
    public DefaultReplyMarkup() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(new KeyboardRow(List.of(
                KeyboardButton.builder().text("/today").build(),
                KeyboardButton.builder().text("/tomorrow").build(),
                KeyboardButton.builder().text("/week").build(),
                KeyboardButton.builder().text("/timetable").build()
        )));
        setResizeKeyboard(true);
        setKeyboard(keyboardRows);
    }

    private DefaultReplyMarkup(@NonNull List<KeyboardRow> keyboard) {
        super(keyboard);
    }

    private DefaultReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}
