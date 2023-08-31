package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.replyMarkup.SetupReplyMarkup;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class SetupCommand implements Command {
    public static SetupCommand instance = new SetupCommand();

    private SetupCommand() {

    }

    @Override
    public String getUsage() {
        return "/setup";
    }

    @Override
    public void execute(Bot bot, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Данный бот не гарантирует точного рассписания. Источник всех данных — schedule.ruc.su. По всем вопросам — @Javapedik");
        bot.parserService.branches().thenAccept(branches -> {
            sendMessage.setReplyMarkup(new SetupReplyMarkup());
            try {
                bot.execute(sendMessage);
                bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.setup);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
