package com.github.javakira.callback;

import com.github.javakira.Bot;
import com.github.javakira.parser.Card;
import com.github.javakira.parser.Cards;
import com.github.javakira.parser.Pair;
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
            String arg = data.split(" ")[1];
            LocalDate localDate = LocalDate.parse(arg);
            EditMessageText editMessageText = new EditMessageText();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(chatId);
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setChatId(chatId);
            getCards(bot, chatId).thenAccept(cards -> {
                StringBuilder builder = new StringBuilder();
                if (!cards.getList().isEmpty()) {
                    builder.append("#Расписание ")
                            .append(bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title())
                            .append("\n");

                    for (int cardIndex = 0; cardIndex < cards.getList().size(); cardIndex++) {
                        Card card = cards.getList().get(cardIndex);
                        builder.append(card.date().getDayOfMonth())
                                .append(".")
                                .append(card.date().getMonth().getValue())
                                .append(".")
                                .append(card.date().getYear())
                                .append(" (")
                                .append(card.date().getDayOfWeek())
                                .append(")\n");
                        for (int i = 0; i < card.pairList().size(); i++) {
                            Pair pair = card.pairList().get(i);
                            builder.append(pair.index())
                                    .append(" — ")
                                    .append(pair.name())
                                    .append("\n")
                                    .append(pair.by())
                                    .append("\n")
                                    .append(pair.place())
                                    .append("\n")
                                    .append(pair.type())
                                    .append("\n\n");
                        }
                    }

                    editMessageText.setText(builder.toString());
                } else {
                    editMessageText.setText("Расписания для " + (bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title()) + " на неделю нет");
                }
            });
            editMessageReplyMarkup.setReplyMarkup(new WeekReplyMarkup(localDate));

            try {
                bot.execute(editMessageText);
                bot.execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {

            }
        }
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
