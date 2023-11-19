package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.parser.ScheduleExceptionHandler;
import com.github.javakira.replyMarkup.SetupReplyMarkup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class SetupCommand implements Command {
    private final ScheduleExceptionHandler exceptionHandler;

    @Override
    public String getUsage() {
        return "/setup";
    }

    @Override
    public void execute(Bot bot, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Данный бот не гарантирует точного рассписания. Источник всех данных — schedule.ruc.su. По всем вопросам — @Javakira");
        bot.parserService.branches().whenComplete((branches, ex) -> {
            exceptionHandler.handle(bot, update.getMessage().getChatId(), ex);
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
