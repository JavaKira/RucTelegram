package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
import java.util.List;

public class SetupReplyMarkup extends ReplyKeyboardMarkup {
    public SetupReplyMarkup() {
        setKeyboard(Collections.singletonList(new KeyboardRow(List.of(KeyboardButton.builder().text("Я понимаю").build()))));
        setResizeKeyboard(true);
    }

    private SetupReplyMarkup(@NonNull List<KeyboardRow> keyboard) {
        super(keyboard);
    }

    private SetupReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}
