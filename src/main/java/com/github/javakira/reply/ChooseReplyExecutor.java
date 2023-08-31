package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.replyMarkup.BranchReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ChooseReplyExecutor implements ReplyExecutor {
    public static ChooseReplyExecutor instance = new ChooseReplyExecutor();

    @Override
    public void execute(Bot bot, Update update) {
        bot.parserService.branches().thenAccept(branches -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Выберите филиал");
            sendMessage.setReplyMarkup(new BranchReplyMarkup(branches, 1));
            bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), 1);
            try {
                bot.execute(sendMessage);
                bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.branch);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
