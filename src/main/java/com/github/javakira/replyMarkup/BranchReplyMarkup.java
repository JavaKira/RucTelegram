package com.github.javakira.replyMarkup;

import com.github.javakira.parser.Branch;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class BranchReplyMarkup extends ReplyKeyboardMarkup {
    public static final int pageSize = 14;

    public BranchReplyMarkup(List<Branch> branches, int page) {
        if (page < 1) throw new IllegalArgumentException("page must be more that 0");

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        if (page != 1)
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text("Предыдущие").build()
            )));

        for (int i = (page - 1) * pageSize; i < Math.min(branches.size(), page * pageSize); i++) {
            keyboardRows.add(new KeyboardRow(List.of(
                KeyboardButton.builder().text(branches.get(i).title()).build()
            )));
        }

        if (page * pageSize < branches.size())
            keyboardRows.add(new KeyboardRow(List.of(
                    KeyboardButton.builder().text("Следующие").build()
            )));

        setResizeKeyboard(true);
        setKeyboard(keyboardRows);
    }

    private BranchReplyMarkup() {
    }

    private BranchReplyMarkup(@NonNull List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, String inputFieldPlaceholder, Boolean isPersistent) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective, inputFieldPlaceholder, isPersistent);
    }
}
