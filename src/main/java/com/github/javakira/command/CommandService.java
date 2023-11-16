package com.github.javakira.command;

import com.github.javakira.Bot;
import com.github.javakira.context.UserContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Service
public class CommandService {
    private final List<Command> commands = new ArrayList<>();

    //todo можно было бы даже заменить на IoC от спринга
    public CommandService() {
        commands.add(StartCommand.instance);
        commands.add(SetupCommand.instance);
        commands.add(TimetableCommand.instance);
        commands.add(TodayCommand.instance);
        commands.add(TomorrowCommand.instance);
        commands.add(WeekCommand.instance);
    }

    public void onUpdateReceived(Bot bot, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String[] split = messageText.split(" ");
            split[0] = split[0].replace("@RucSchedule_bot", "");

            bot.userContextService.update(update.getMessage().getFrom());

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
