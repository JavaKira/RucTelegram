package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.replyMarkup.DefaultReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand implements Command {
    public static final StartCommand instance = new StartCommand();

    private StartCommand() {

    }

    @Override
    public String getUsage() {
        return "/start";
    }

    @Override
    public void execute(Bot bot, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Чтобы начать, настрой бота, используя /setup. Получить расписание можно используя /today, /tomorrow и /schedule");
        sendMessage.setReplyMarkup(new DefaultReplyMarkup());
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
