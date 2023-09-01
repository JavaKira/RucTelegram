package com.github.javakira.callback;

import com.github.javakira.Bot;
import com.github.javakira.replyMarkup.WeekReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Date;

public class WeekCallbackConsumer extends CallbackConsumer {
    public static WeekCallbackConsumer instance = new WeekCallbackConsumer();

    @Override
    public void accept(Bot bot, Update update) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        long chatId = message.getChatId();
        String data = query.getData();

        if (data.startsWith("week")) {
            String arg = data.split(" ")[1];
            LocalDate localDate = LocalDate.parse(arg);
            EditMessageText editMessageText = new EditMessageText();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(chatId);
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setChatId(chatId);
            editMessageText.setText("Думал шяс расписание на неделю получишь? Рано радуешься.");
            editMessageReplyMarkup.setReplyMarkup(new WeekReplyMarkup(localDate));

            try {
                bot.execute(editMessageText);
                bot.execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {

            }
        }
    }
}
