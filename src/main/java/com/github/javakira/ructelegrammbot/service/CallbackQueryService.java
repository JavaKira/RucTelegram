package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.Bot;
import com.github.javakira.ructelegrammbot.model.Group;
import com.github.javakira.ructelegrammbot.model.Kit;
import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class CallbackQueryService implements BotService {
    private final Map<String, Consumer<CallbackQuery>> callbackQueryConsumers = new HashMap<>();

    public void putCallbackQueryConsumer(String command, Consumer<CallbackQuery> action) {
        callbackQueryConsumers.put(command, action);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String[] split = update.getCallbackQuery().getData().split(" ");

            if (callbackQueryConsumers.containsKey(split[0]))
                callbackQueryConsumers.get(split[0]).accept(
                        new CallbackQuery(update, update.getCallbackQuery().getData().replace(split[0], "").trim())
                );
        }
    }

    public record CallbackQuery(Update update, String data) {

    }
}
