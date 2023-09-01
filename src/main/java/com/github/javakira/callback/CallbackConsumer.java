package com.github.javakira.callback;

import com.github.javakira.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

public abstract class CallbackConsumer {
    public abstract void accept(Bot bot, Update update);
}
