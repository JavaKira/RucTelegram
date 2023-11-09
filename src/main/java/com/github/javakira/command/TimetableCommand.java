package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.replyMarkup.DefaultReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimetableCommand implements Command {
    public static TimetableCommand instance = new TimetableCommand();

    private TimetableCommand() {

    }

    @Override
    public String getUsage() {
        return "/timetable";
    }

    @Override
    public void execute(Bot bot, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(
                """
                        Расписание звонков пар:
                        1 пара — 8:30–9:15 9:25–10:10
                        2 пара — 10:30–11:15 11:25–12:10
                        3 пара — 12:30–13:15 13:25–14:10
                        4 пара — 14:30–15:15 15:25–16:10
                        5 пара — 16:20–17:05 17:15–18:00
                        6 пара — 18:10–19:40
                        7 пара — 19:50–21:20
                        """
        );
        sendMessage.setReplyMarkup(new DefaultReplyMarkup());
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
