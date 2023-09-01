package com.github.javakira.callback;

import com.github.javakira.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class WeekCallbackConsumer extends CallbackConsumer {
    public static WeekCallbackConsumer instance = new WeekCallbackConsumer();

    @Override
    public void accept(Bot bot, Update update) {

    }
}
