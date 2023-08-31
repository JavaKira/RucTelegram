package com.github.javakira.replyMarkup;

import com.github.javakira.parser.Group;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class GroupReplyMarkup extends ReplyKeyboardMarkup {
    public static final int pageSize = 14;

    public GroupReplyMarkup(List<Group> groups, int page) {
        if (page < 1) throw new IllegalArgumentException("page must be more that 0");

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        if (page != 1)
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text("Предыдущие").build()
            )));

        for (int i = (page - 1) * pageSize; i < Math.min(groups.size(), page * pageSize); i++) {
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text(groups.get(i).title()).build()
            )));
        }

        if (page * pageSize < groups.size())
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text("Следующие").build()
            )));

        setResizeKeyboard(true);
        setKeyboard(keyboardRows);
    }

    public GroupReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}
