package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.context.ChatContext;
import com.github.javakira.parser.Card;
import com.github.javakira.parser.Cards;
import com.github.javakira.parser.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TodayCommand implements Command {
    public static TodayCommand instance = new TodayCommand();

    private TodayCommand() {

    }

    @Override
    public String getUsage() {
        return "/today";
    }

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        LocalDate today = LocalDate.now();
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
            builder.append("#Расписание ")
                    .append(bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title())
                    .append(" на ")
                    .append(today.getDayOfMonth())
                    .append(".")
                    .append(today.getMonth().getValue())
                    .append(".")
                    .append(today.getYear())
                    .append(" (")
                    .append(today.getDayOfWeek())
                    .append(")\n");
            Optional<Card> optionalCard = cards.today();
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
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

                sendMessage.setText(builder.toString());
            } else {
                sendMessage.setText("Расписания для " + (bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title()) + " на сегодня нет");
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
