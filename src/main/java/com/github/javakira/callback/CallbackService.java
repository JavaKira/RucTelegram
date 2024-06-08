package com.github.javakira.callback;

import com.github.javakira.Bot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class CallbackService {
    private final List<CallbackConsumer> consumers = new ArrayList<>();

    @Autowired
    public CallbackService(AdminDistributionCallbackConsumer consumer) {
        consumers.add(WeekCallbackConsumer.instance);
        consumers.add(consumer);
    }

    public void onUpdateReceived(Bot bot, Update update) {
        if (update.hasCallbackQuery())
            consumers.forEach(consumer -> consumer.accept(bot, update));
    }
}
