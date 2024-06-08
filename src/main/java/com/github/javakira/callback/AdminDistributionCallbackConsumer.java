package com.github.javakira.callback;

import com.github.javakira.Bot;
import com.github.javakira.admin.AdminService;
import com.github.javakira.context.ChatContextService;
import com.github.javakira.context.ReplyState;
import com.github.javakira.context.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AdminDistributionCallbackConsumer extends CallbackConsumer {
    private final AdminService service;
    private final ChatContextService chatContextService;
    private final UserContextService userContextService;

    @Override
    public void accept(Bot bot, Update update) {
        CallbackQuery query = update.getCallbackQuery();
        Message message = query.getMessage();
        long chatId = message.getChatId();
        long userId = query.getFrom().getId();
        String data = query.getData();

        if (data.startsWith("admin_distribution_ok")) {
            if (!service.isAdmin(userId)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Данная опция доступна только админам");
                try {
                    bot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

                return;
            }

            SendMessage ok = new SendMessage();
            ok.setChatId(chatId);
            ok.setText("Начинаю рассылку");
            try {
                bot.execute(ok);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

            userContextService.getAllChats().forEach(id -> {
                service.sendDistributionMessage(bot, id);
            });
            service.getQuery().addLast(() -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(
                        "Сообщение успешно отправлено " +
                                service.getCounter() +
                                " из " +
                                userContextService.getAllChats().size() +
                                " пользователям за" +
                                (service.getTimeStart() - System.currentTimeMillis())
                );

                try {
                    bot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
            service.startDistributionQuery();

            return;
        }

        if (data.startsWith("admin_distribution")) {
            if (!service.isAdmin(userId)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Данная опция доступна только админам");
                try {
                    bot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

                return;
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Введи сообщение");
            sendMessage.setReplyMarkup(null);
            chatContextService.replyState(chatId, ReplyState.distribution);
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
