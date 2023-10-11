package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.parser.Card;
import com.github.javakira.parser.Cards;
import com.github.javakira.parser.Pair;
import com.github.javakira.util.Formatter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TomorrowCommand implements Command {
    public static TomorrowCommand instance = new TomorrowCommand();

    private TomorrowCommand() {

    }

    @Override
    public String getUsage() {
        return "/tomorrow";
    }

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
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
            StringBuilder builder = new StringBuilder();
            builder.append("#Расписание ")
                    .append(bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title())
                    .append(" на <b>")
                    .append(tomorrow.getDayOfMonth())
                    .append(".")
                    .append(tomorrow.getMonth().getValue())
                    .append(".")
                    .append(tomorrow.getYear())
                    .append(" (")
                    .append(Formatter.formatDayOfWeek(tomorrow.getDayOfWeek()))
                    .append(")</b>\n");
            Optional<Card> optionalCard = cards.tomorrow();
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                for (int i = 0; i < card.pairList().size(); i++) {
                    Pair pair = card.pairList().get(i);
                    builder.append("<b>")
                            .append(pair.index())
                            .append(" — ")
                            .append(pair.name())
                            .append("</b>\n")
                            .append(pair.by())
                            .append("\n")
                            .append(pair.place())
                            .append("\n")
                            .append(pair.type())
                            .append("\n\n");
                }

                sendMessage.setText(builder.toString());
            } else {
                sendMessage.setText("Расписания для " + (bot.chatContextService.isEmployee(chatId) ? bot.chatContextService.employee(chatId).title() : bot.chatContextService.group(chatId).title()) + " на завтра нет");
            }

            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Cards> getCards(Bot bot, long chatId) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        if (!bot.chatContextService.isEmployee(chatId)) {
            return bot.parserService.groupCards(
                    bot.chatContextService.branch(chatId).value(),
                    bot.chatContextService.kit(chatId).value(),
                    bot.chatContextService.group(chatId).value(),
                    tomorrow
            );
        } else {
            return bot.parserService.employeeCards(
                    bot.chatContextService.branch(chatId).value(),
                    bot.chatContextService.employee(chatId).value(),
                    tomorrow
            );
        }
    }
}
