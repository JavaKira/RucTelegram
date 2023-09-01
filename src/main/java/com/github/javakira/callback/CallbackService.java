package com.github.javakira.callback;

import com.github.javakira.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class CallbackService {
    private final List<CallbackConsumer> consumers = new ArrayList<>();

    public void onUpdateReceived(Bot bot, Update update) {
        consumers.forEach(consumer -> consumer.accept(bot, update));
    }
}
