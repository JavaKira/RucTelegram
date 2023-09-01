package com.github.javakira.reply;

import com.github.javakira.Bot;
import com.github.javakira.context.ReplyState;
import com.github.javakira.parser.Branch;
import com.github.javakira.parser.Employee;
import com.github.javakira.parser.Group;
import com.github.javakira.parser.Kit;
import com.github.javakira.replyMarkup.BranchReplyMarkup;
import com.github.javakira.replyMarkup.EmployeeReplyMarkup;
import com.github.javakira.replyMarkup.GroupReplyMarkup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Service
@Slf4j
public class ReplyExecutorService {
    public void onUpdateReceived(Bot bot, Update update) {
        if (!update.hasMessage())
            return;

        ReplyState replyState = bot.chatContextService.replyState(update.getMessage().getChatId());
        switch (replyState) {
            case def -> {}
            case setup -> setup(bot, update);
            case choose -> choose(bot, update);
            case branch -> branch(bot, update);
            case kit -> kit(bot, update);
            case group -> group(bot, update);
            case employee -> employee(bot, update);
        }
    }

    public void setup(Bot bot, Update update) {
        String text = update.getMessage().getText();
        if (text.equals("Я понимаю"))
            SetupReplyExecutor.instance.execute(bot, update);
    }

    public void choose(Bot bot, Update update) {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        boolean employee = text.equals("Работник");
        boolean student = text.equals("Студент");
        if (employee || student) {
            if (employee)
                bot.chatContextService.isEmployee(chatId, true);

            if (student)
                bot.chatContextService.isEmployee(chatId, false);
        }

        ChooseReplyExecutor.instance.execute(bot, update);
    }

    public void branch(Bot bot, Update update) {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        boolean prev = text.equals("Предыдущие");
        boolean next = text.equals("Следующие");
        int lastReplyPage = bot.chatContextService.lastReplyPage(chatId);
        if (prev || next) {
            int newPage = next ? lastReplyPage + 1 : lastReplyPage - 1;
            bot.parserService.branches().thenAccept(branches -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Выберите филиал (страница " + newPage + "/" + ((branches.size() / BranchReplyMarkup.pageSize) + 1) + ")");
                sendMessage.setReplyMarkup(new BranchReplyMarkup(branches, newPage));
                bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), newPage);
                try {
                    bot.execute(sendMessage);
                    bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.branch);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });

            return;
        }

        bot.parserService.branches().thenAccept(branches -> {
            Optional<Branch> branchOptional = branches.stream().filter(branch -> branch.title().equals(text)).findAny();
            if (branchOptional.isPresent()) {
                BranchReplyExecutor.instance.branch = branchOptional.get();
                BranchReplyExecutor.instance.isEmployee = bot.chatContextService.isEmployee(chatId);
                BranchReplyExecutor.instance.execute(bot, update);
            }
        });
    }

    public void kit(Bot bot, Update update) {
        String text = update.getMessage().getText();
        bot.parserService.kits(bot.chatContextService.branch(update.getMessage().getChatId())).thenAccept(kits -> {
            Optional<Kit> kitOptional = kits.stream().filter(kit -> kit.title().equals(text)).findAny();
            if (kitOptional.isPresent()) {
                KitReplyExecutor.instance.kit = kitOptional.get();
                KitReplyExecutor.instance.execute(bot, update);
            }
        });
    }

    public void employee(Bot bot, Update update) {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        boolean prev = text.equals("Предыдущие");
        boolean next = text.equals("Следующие");
        int lastReplyPage = bot.chatContextService.lastReplyPage(chatId);
        if (prev || next) {
            int newPage = next ? lastReplyPage + 1 : lastReplyPage - 1;
            bot.parserService.employee(bot.chatContextService.branch(chatId)).thenAccept(employees -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Выберите работника (страница " + newPage + "/" + ((employees.size() / BranchReplyMarkup.pageSize) + 1) + ")");
                sendMessage.setReplyMarkup(new EmployeeReplyMarkup(employees, next ? lastReplyPage + 1 : lastReplyPage - 1));
                bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), next ? lastReplyPage + 1 : lastReplyPage - 1);
                try {
                    bot.execute(sendMessage);
                    bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.employee);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });

            return;
        }

        bot.parserService.employee(bot.chatContextService.branch(chatId)).thenAccept(employees -> {
            Optional<Employee> employeeOptional = employees.stream().filter(employee -> employee.title().equals(text)).findAny();
            if (employeeOptional.isPresent()) {
                EmployeeReplyExecutor.instance.employee = employeeOptional.get();
                EmployeeReplyExecutor.instance.execute(bot, update);
            }
        });
    }

    public void group(Bot bot, Update update) {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        boolean prev = text.equals("Предыдущие");
        boolean next = text.equals("Следующие");
        int lastReplyPage = bot.chatContextService.lastReplyPage(chatId);
        if (prev || next) {
            bot.parserService.groups(
                    bot.chatContextService.branch(chatId),
                    bot.chatContextService.kit(chatId)
            ).thenAccept(groups -> {
                int newPage = next ? lastReplyPage + 1 : lastReplyPage - 1;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Выберите группу (страница " + newPage + "/" + ((groups.size() / BranchReplyMarkup.pageSize) + 1) + ")");
                sendMessage.setReplyMarkup(new GroupReplyMarkup(groups, next ? lastReplyPage + 1 : lastReplyPage - 1));
                bot.chatContextService.lastReplyPage(update.getMessage().getChatId(), next ? lastReplyPage + 1 : lastReplyPage - 1);
                try {
                    bot.execute(sendMessage);
                    bot.chatContextService.replyState(update.getMessage().getChatId(), ReplyState.group);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });

            return;
        }

        bot.parserService.groups(
                bot.chatContextService.branch(chatId),
                bot.chatContextService.kit(update.getMessage().getChatId())
        ).thenAccept(groups -> {
            Optional<Group> groupOptional = groups.stream().filter(group -> group.title().equals(text)).findAny();
            if (groupOptional.isPresent()) {
                GroupReplyExecutor.instance.group = groupOptional.get();
                GroupReplyExecutor.instance.execute(bot, update);
            }
        });
    }
}
