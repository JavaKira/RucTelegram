package com.github.javakira.replyMarkup;

import com.github.javakira.parser.Kit;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KitReplyMarkup extends ReplyKeyboardMarkup {

    public KitReplyMarkup(List<Kit> kits) {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        for (int i = 0; i < kits.size(); i++) {
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text(kits.get(i).title()).build()
            )));
        }

        setResizeKeyboard(true);
        setKeyboard(keyboardRows);
    }

    private KitReplyMarkup() {
    }

    private KitReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}