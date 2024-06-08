package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.admin.AdminService;
import com.github.javakira.replyMarkup.AdminReplyMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class AdminCommand implements Command {
    private final AdminService service;

    @Override
    public String getUsage() {
        return "/admin";
    }

    @Override
    public void execute(Bot bot, Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        if (service.isAdmin(userId)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Выберите действие");
            sendMessage.setReplyMarkup(new AdminReplyMarkup());
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Данная команда доступна только админам");
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
