package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.parser.Group;
import com.github.javakira.replyMarkup.DefaultReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GroupReplyExecutor implements ReplyExecutor {
    public static GroupReplyExecutor instance = new GroupReplyExecutor();

    public Group group;

    private GroupReplyExecutor() {

    }

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        bot.chatContextService.group(update.getMessage().getChatId(), group);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(
                "Настройки чата:\n" +
                        bot.chatContextService.branch(chatId).title() + "\n" +
                        bot.chatContextService.kit(chatId).title() + "\n" +
                        bot.chatContextService.group(chatId).title() + "\n\n" +
                        "Теперь бот готов к работе. Используй /today, /tomorrow или /week чтобы получить рассписание"
        );
        sendMessage.setReplyMarkup(new DefaultReplyMarkup());
        try {
            bot.execute(sendMessage);
            bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.def);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
