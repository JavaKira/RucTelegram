package com.github.javakira.replyMarkup;

import lombok.NonNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class AdminDistributionReplyMarkup extends InlineKeyboardMarkup {
    public AdminDistributionReplyMarkup() {
        setKeyboard(
                List.of(
                        List.of(InlineKeyboardButton.builder().text("Да все верно").callbackData("admin_distribution_ok").build())
                )
        );
    }



    private AdminDistributionReplyMarkup(@NonNull List<List<InlineKeyboardButton>> keyboard) {
        super(keyboard);
    }
}
