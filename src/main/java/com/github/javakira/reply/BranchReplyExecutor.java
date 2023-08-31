package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.parser.Branch;
import com.github.javakira.replyMarkup.EmployeeReplyMarkup;
import com.github.javakira.replyMarkup.KitReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BranchReplyExecutor implements ReplyExecutor {
    public static BranchReplyExecutor instance = new BranchReplyExecutor();

    public Branch branch;
    public boolean isEmployee;

    private BranchReplyExecutor() {

    }

    @Override
    public void execute(Bot bot, Update update) {
        bot.chatContextService.branch(update.getMessage().getChatId(), branch);
        if (isEmployee) {
            bot.parserService.employee(branch).thenAccept(employees -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId());
                sendMessage.setText("Выберите работника");
                sendMessage.setReplyMarkup(new EmployeeReplyMarkup(employees, 1));
                bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), 1);
                try {
                    bot.execute(sendMessage);
                    bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.employee);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            bot.parserService.kits(branch).thenAccept(kit -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId());
                sendMessage.setText("Выберите набор");
                sendMessage.setReplyMarkup(new KitReplyMarkup(kit));
                try {
                    bot.execute(sendMessage);
                    bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.kit);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
