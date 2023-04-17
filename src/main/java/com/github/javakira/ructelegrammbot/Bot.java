package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.config.BotConfig;
import com.github.javakira.ructelegrammbot.model.Branch;
import com.github.javakira.ructelegrammbot.model.Group;
import com.github.javakira.ructelegrammbot.model.Kit;
import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.model.statistic.CommandUsageStatistic;
import com.github.javakira.ructelegrammbot.parser.HtmlScheduleParser;
import com.github.javakira.ructelegrammbot.parser.ScheduleParser;
import com.github.javakira.ructelegrammbot.service.*;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
    final BotConfig config;

    @Autowired
    private SettingsService service;
    @Autowired
    private CommandService commandService;
    @Autowired
    private CallbackQueryService callbackQueryService;
    @Autowired
    private SendService sendService;
    @Autowired
    private CommandUsageStatisticService commandUsageStatisticService;

    public Bot(BotConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        registerCommands();
        registerCallbackQueryConsumers();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        commandService.onUpdateReceived(update);
        callbackQueryService.onUpdateReceived(update);
    }

    private void registerCommands() {
        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            if (!service.isSettingsExist4Chat(chatId)) {
                executeSendMessage(sendService.sendWarning(chatId, update.getMessage()));
            } else {
                sendService.sendBranches(chatId, update.getMessage()).thenAccept(this::executeSendMessage);
            }
        }, "/настроить", "/setup");

        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            boolean checked = checkSettings(chatId);
            if (checked)
                sendService.scheduleToday(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            else
                executeSendMessage(sendService.sendNotConfigured(chatId));
        }, "/сегодня", "/today");

        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            boolean checked = checkSettings(chatId);
            if (checked)
                sendService.scheduleTomorrow(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            else
                executeSendMessage(sendService.sendNotConfigured(chatId));
        }, "/завтра", "/tomorrow");
    }

    private void registerCallbackQueryConsumers() {
        registerCallbackQueryConsumer("warning", query -> {
            Message message = query.update().getCallbackQuery().getMessage();
            service.createSettings(message.getChat());
            clearKeyboard(message);
            Message setupMessage = new Message();
            setupMessage.setMessageId(Integer.valueOf(query.data()));
            sendService.sendBranches(message.getChatId(), setupMessage).thenAccept(this::executeSendMessage);
        });

        registerCallbackQueryConsumer("branch", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            clearKeyboard(query.update().getCallbackQuery().getMessage());
            ScheduleParser scheduleParser = new HtmlScheduleParser();
            scheduleParser.getBranches().thenAccept(branches -> {
                Branch branch = branches.stream().filter(branch1 -> branch1.value().equals(query.data())).findFirst().orElseThrow();
                service.setBranch(chatId, branch);
                sendService.sendKits(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            });
        });

        registerCallbackQueryConsumer("kit", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            ScheduleParser scheduleParser = new HtmlScheduleParser();
            scheduleParser.getKits(service.getSettings(chatId).getBranch()).thenAccept(kits -> {
                Kit kit = kits.stream().filter(kit1 -> kit1.value().equals(query.data())).findFirst().orElseThrow();

                //todo подумать над оптимизацией - скачивать список групп слишком жирно
                scheduleParser.getGroups(service.getSettings(chatId).getBranch(), kit.value()).thenAccept(groups -> {
                    if (groups.isEmpty()) {
                        executeSendMessage(sendService.sendString(chatId, kit.title() + " недоступен"));
                    } else {
                        clearKeyboard(query.update().getCallbackQuery().getMessage());
                        service.setKit(chatId, kit);
                        createGroupsKeyboard(chatId, 0).thenAccept(keyboardMarkup -> {
                            executeSendMessage(sendService.sendGroups(chatId, keyboardMarkup));
                        });
                    }
                });
            });
        });

        registerCallbackQueryConsumer("group", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            clearKeyboard(query.update().getCallbackQuery().getMessage());
            ScheduleParser scheduleParser = new HtmlScheduleParser();
            scheduleParser.getGroups(
                    service.getSettings(chatId).getBranch(),
                    service.getSettings(chatId).getKit()
            ).thenAccept(groups -> {
                Group group = groups.stream().filter(group1 -> group1.value().equals(query.data())).findFirst().orElseThrow();
                service.setGroup(chatId, group);
                executeSendMessage(sendService.sendSettings(chatId, service.getSettings(chatId)));
            });
        });

        registerCallbackQueryConsumer("groupnext", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(query.update().getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setChatId(chatId);
            createGroupsKeyboard(chatId, Integer.parseInt(query.data()) + 1).thenAccept(inlineKeyboardMarkup -> {
                editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        registerCallbackQueryConsumer("groupprev", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(query.update().getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setChatId(chatId);
            createGroupsKeyboard(chatId, Integer.parseInt(query.data()) - 1).thenAccept(inlineKeyboardMarkup -> {
                editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void registerCommand(String command, Consumer<Update> action) {
        commandService.putCommand(command, update -> {
            Chat chat = update.getMessage().getChat();
            commandUsageStatisticService.add(CommandUsageStatistic.builder()
                            .chatFirstName(chat.getFirstName())
                            .chatLastName(chat.getLastName())
                            .chatUsername(chat.getUserName())
                            .chatTitle(chat.getTitle())
                            .command(command)
                            .date(new Date())
                            .setSettings(service.getSettings(chat.getId()))
                            .userUsername(update.getMessage().getFrom().getUserName())
                            .build());
            action.accept(update);
        });
    }

    private void registerCommands(Consumer<Update> action, String... commands) {
        for (String command : commands)
            registerCommand(command, action);
    }

    private void registerCallbackQueryConsumer(String command, Consumer<CallbackQueryService.CallbackQuery> action) {
        callbackQueryService.putCallbackQueryConsumer(command, action);
    }

    //todo можно переместить в SettingsService
    private boolean checkSettings(long chatId) {
        if (!service.isSettingsExist4Chat(chatId)) {
            return false;
        } else {
            Settings settings = service.getSettings(chatId);
            if (settings.getBranch() == null)
                return false;

            if (settings.isEmployee())
                return settings.getEmployeeKey() != null;
            else {
                if (settings.getKit() == null) {
                    return false;
                }

                return settings.getGroupKey() != null;
            }
        }
    }

    private void clearKeyboard(Message message) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setMessageId(message.getMessageId());
        editMessageReplyMarkup.setChatId(message.getChatId());
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<InlineKeyboardMarkup> createGroupsKeyboard(long chatId, int page) {
        ScheduleParser scheduleParser = new HtmlScheduleParser();
        return scheduleParser.getGroups(
                service.getSettings(chatId).getBranch(),
                service.getSettings(chatId).getKit()
        ).thenApply(groups -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            List<InlineKeyboardButton> buttons1 = new LinkedList<>();
            if (page != 0)
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("предыдущее")
                                .callbackData("groupprev " + page)
                                .build()
                );
            for (int i = page * 14; i < page * 14 + 14; i++) {
                Group group = groups.get(i);
                buttons1.add(InlineKeyboardButton.builder()
                        .text(group.title())
                        .callbackData("group " + group.value())
                        .build());

                if (buttons1.size() >= 2) {
                    buttons.add(buttons1);
                    buttons1 = new LinkedList<>();
                }
            }
            if ((page * 14 + 14) < groups.size()) {
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("следующее")
                                .callbackData("groupnext " + page)
                                .build()
                );
                buttons.add(buttons1);
            }

            return new InlineKeyboardMarkup(buttons);
        });
    }

    private void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage() + " : " + sendMessage);
        }
    }
}