package com.github.javakira.callback;

import com.github.javakira.Bot;
import com.github.javakira.command.WeekTextBuilder;
import com.github.javakira.api.Cards;
import com.github.javakira.replyMarkup.WeekReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class WeekCallbackConsumer extends CallbackConsumer {
    public static WeekCallbackConsumer instance = new WeekCallbackConsumer();

    @Override
    public void accept(Bot bot, Update update) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        long chatId = message.getChatId();
        String data = query.getData();

        if (data.startsWith("week")) {
            rebuildMessage(bot, message, chatId, data);
        }

        if (data.startsWith("weekScheduleShorted_false")) {
            bot.chatContextService.isWeekScheduleShorted(chatId, false);
            rebuildMessage(bot, message, chatId, data);
        } else if (data.startsWith("weekScheduleShorted_true")) {
            bot.chatContextService.isWeekScheduleShorted(chatId, true);
            rebuildMessage(bot, message, chatId, data);
        }
    }

    private void rebuildMessage(Bot bot, Message message, long chatId, String data) {
        String arg = data.split(" ")[1];
        LocalDate localDate = LocalDate.parse(arg);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setParseMode("HTML");
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(message.getMessageId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setChatId(chatId);
        getCards(bot, chatId, localDate).thenAccept(cards -> {
            String title = bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title();
            editMessageText.setText(new WeekTextBuilder(cards, title, bot.chatContextService.isWeekScheduleShorted(chatId)).text());
            editMessageReplyMarkup.setReplyMarkup(new WeekReplyMarkup(bot.chatContextService.isWeekScheduleShorted(chatId), localDate));

            try {
                bot.execute(editMessageText);
                bot.execute(editMessageReplyMarkup);
            } catch (TelegramApiException ignored) {

            }
        });
    }

    private CompletableFuture<Cards> getCards(Bot bot, long chatId, LocalDate today) {
        if (!bot.chatContextService.isEmployee(chatId)) {
            return bot.parserService.groupCards(
                    bot.chatContextService.branch(chatId).value(),
                    bot.chatContextService.kit(chatId).value(),
                    bot.chatContextService.group(chatId).value(),
                    today
            );
        } else {
            return bot.parserService.employeeCards(
                    bot.chatContextService.branch(chatId).value(),
                    bot.chatContextService.employee(chatId).value(),
                    today
            );
        }
    }
}
