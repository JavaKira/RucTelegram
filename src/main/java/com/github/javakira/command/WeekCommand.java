package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.parser.Card;
import com.github.javakira.parser.Cards;
import com.github.javakira.parser.Pair;
import com.github.javakira.replyMarkup.WeekReplyMarkup;
import com.github.javakira.util.Formatter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class WeekCommand implements Command {
    public static WeekCommand instance = new WeekCommand();

    private WeekCommand() {

    }

    @Override
    public String getUsage() {
        return "/week";
    }

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        if (!bot.chatContextService.isSetup(chatId)) {
            sendMessage.setText("Бот не настроен, используйте /setup");
            try {
                bot.execute(sendMessage);
                return;
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        getCards(bot, chatId).thenAccept(cards -> {
            String title = bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title();
            sendMessage.setText(new WeekTextBuilder(cards, title).text());
            sendMessage.setReplyMarkup(new WeekReplyMarkup());

            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Cards> getCards(Bot bot, long chatId) {
        LocalDate today = LocalDate.now();

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
