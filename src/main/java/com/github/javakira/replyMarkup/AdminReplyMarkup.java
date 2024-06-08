package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class AdminReplyMarkup extends InlineKeyboardMarkup {
    public AdminReplyMarkup() {
        setKeyboard(
                List.of(
                        List.of(InlineKeyboardButton.builder().text("Рассылка всем пользователям").callbackData("admin_distribution").build())
                )
        );
    }

    private AdminReplyMarkup(@NonNull List<List<InlineKeyboardButton>> keyboard) {
        super(keyboard);
    }
}
