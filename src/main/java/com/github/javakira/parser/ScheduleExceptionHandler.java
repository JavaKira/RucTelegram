package com.github.javakira.parser;

import com.github.javakira.Bot;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletionException;

@Component
public class ScheduleExceptionHandler {
    public void handle(@NonNull Bot bot, @NonNull Long chatId, Throwable e) {
        if (e == null)
            return;

        if (e instanceof CompletionException)
            e = e.getCause();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (e instanceof ServerNotRespondingException)
            sendMessage.setText("Сервер не отвечает, повторите команду позже");
        else
            sendMessage.setText("Было выброшено " + e);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }

        throw new RuntimeException(e);
    }
}
