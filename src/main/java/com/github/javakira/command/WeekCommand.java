package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.parser.Card;
import com.github.javakira.parser.Cards;
import com.github.javakira.parser.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

                sendMessage.setText(builder.toString());
            } else {
                sendMessage.setText("Расписания для " + (bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title()) + " на неделю нет");
            }

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
