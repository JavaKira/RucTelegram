package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.parser.Kit;
import com.github.javakira.replyMarkup.DefaultReplyMarkup;
import com.github.javakira.replyMarkup.GroupReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class KitReplyExecutor implements ReplyExecutor {
    public static KitReplyExecutor instance = new KitReplyExecutor();

    public Kit kit;

    private KitReplyExecutor() {

    }

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        bot.chatContextService.kit(chatId, kit);
        bot.parserService.groups(bot.chatContextService.branch(chatId), kit).thenAccept(groups -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Выберите группу");
            sendMessage.setReplyMarkup(new GroupReplyMarkup(groups, 1));
            bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), 1);
            try {
                bot.execute(sendMessage);
                bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.group);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
