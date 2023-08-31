package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class ChooseReplyMarkup extends ReplyKeyboardMarkup {
    public ChooseReplyMarkup() {
        setKeyboard(List.of(
                new KeyboardRow(List.of(KeyboardButton.builder().text("Студент").build())),
                new KeyboardRow(List.of(KeyboardButton.builder().text("Работник").build()))
        ));
        setResizeKeyboard(true);
    }

    private ChooseReplyMarkup(@NonNull List<KeyboardRow> keyboard) {
        super(keyboard);
    }

    private ChooseReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}

