package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.replyMarkup.BranchReplyMarkup;
import com.github.javakira.replyMarkup.ChooseReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SetupReplyExecutor implements ReplyExecutor {
    public static SetupReplyExecutor instance = new SetupReplyExecutor();

    private SetupReplyExecutor() {

    }

    @Override
    public void execute(Bot bot, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Кто ты войн?");
        sendMessage.setReplyMarkup(new ChooseReplyMarkup());
        try {
            bot.execute(sendMessage);
            bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.choose);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
