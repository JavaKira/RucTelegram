package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.admin.AdminService;
import com.github.javakira.context.ChatContextService;
import com.github.javakira.context.ReplyState;
import com.github.javakira.context.UserContextService;
import com.github.javakira.replyMarkup.AdminDistributionReplyMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class DistributionReplyExecutor implements ReplyExecutor {
    private final AdminService adminService;
    private final ChatContextService chatContextService;
    private final UserContextService userContextService;

    @Override
    public void execute(Bot bot, Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        Message message = update.getMessage();
        boolean hasPhoto = message.hasPhoto();

        if (!adminService.isAdmin(userId)) {
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

        chatContextService.replyState(chatId, ReplyState.def);
        adminService.getQuery().clear();
        adminService.setDistributionMessage(message);
        adminService.sendDistributionMessage(bot, chatId);
        adminService.startDistributionQuery();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Разослать " + userContextService.getAllChats().size() + " пользователям бота?");
        sendMessage.setReplyMarkup(new AdminDistributionReplyMarkup());
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
