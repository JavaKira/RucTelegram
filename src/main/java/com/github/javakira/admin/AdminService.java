package com.github.javakira.admin;

import com.github.javakira.Bot;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository repository;

    private Message distributionMessage;

    private final LinkedList<Runnable> query = new LinkedList<>();
    private long timeStart;
    private int counter;

    public boolean isAdmin(long userId) {
        return repository.existsById(userId);
    }

    public void sendDistributionMessage(Bot bot, long chatId) {
        if (distributionMessage.hasPhoto()) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile().setMedia(distributionMessage.getPhoto().get(0).getFileId()));
            sendPhoto.setCaptionEntities(distributionMessage.getCaptionEntities());
            sendPhoto.setCaption(distributionMessage.getCaption());

            query.addLast(() -> {
                try {
                    bot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    log.error(e.toString());
                }
            });
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setEntities(distributionMessage.getEntities());
            sendMessage.setText(distributionMessage.getText());

            query.addLast(() -> {
                try {
                    bot.execute(sendMessage);
                    log.info("Рассылка: {} для {} count {}", sendMessage.getText().substring(0, Math.min(sendMessage.getText().length(), 15)), chatId, counter);
                } catch (TelegramApiException e) {
                    log.error(e.toString());
                }
            });
        }
    }

    public void startDistributionQuery() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try (Closeable close = service::shutdown) {
            timeStart = System.currentTimeMillis();
            counter = 0;
            service.execute(() -> {
                while (!query.isEmpty()) {
                    Runnable pop = query.pop();
                    pop.run();
                    counter++;
                    try {
                        Thread.sleep(1000);
                        Thread.yield();
                    } catch (InterruptedException e) {
                        log.error(e.toString());
                    }
                }
            });
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
