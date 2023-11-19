package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Service
public class CommandService {
    private final List<Command> commands = new ArrayList<>();

    @Autowired
    public CommandService(List<Command> commands) {
        this.commands.addAll(commands);
    }

    public void onUpdateReceived(Bot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String[] split = messageText.split(" ");
            split[0] = split[0].replace("@RucSchedule_bot", "");

            bot.userContextService.update(
                    update.getMessage().getFrom(),
                    update.getMessage().getChat().isUserChat() ? update.getMessage().getChatId() : null
            );

            Optional<Command> usedCommand = commands.stream().filter(command -> command.getUsage().equals(split[0])).findAny();
            usedCommand.ifPresent(command -> {
                command.execute(bot, update);
                bot.statisticService.addCommandUsage(
                        command.getUsage(),
                        update.getMessage().getChat(),
                        bot.chatContextService.get(update.getMessage().getChatId())
                );
            });
        }
    }
}
