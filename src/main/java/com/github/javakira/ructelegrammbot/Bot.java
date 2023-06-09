package com.github.javakira.ructelegrammbot;

import com.github.javakira.ructelegrammbot.config.BotConfig;
import com.github.javakira.ructelegrammbot.parser.*;
import com.github.javakira.ructelegrammbot.settings.Settings;
import com.github.javakira.ructelegrammbot.settings.SettingsService;
import com.github.javakira.ructelegrammbot.statistic.CallbackUsageStatistic;
import com.github.javakira.ructelegrammbot.statistic.CommandUsageStatistic;
import com.github.javakira.ructelegrammbot.service.*;
import com.github.javakira.ructelegrammbot.statistic.StatisticService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.*;
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
    private StatisticService statisticService;
    @Autowired
    private ParserService parserService;

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
        try {
            commandService.onUpdateReceived(update);
            callbackQueryService.onUpdateReceived(update);
        } catch (Exception e) {
            if (update.hasMessage() && update.getMessage().hasText())
                executeSendMessage(sendService.sendException(update.getMessage().getChatId(), e));
            else if (update.hasCallbackQuery()) {
                executeSendMessage(sendService.sendException(update.getCallbackQuery().getMessage().getChatId(), e));
            }
        }
    }

    private void registerCommands() {
        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            if (!service.isSettingsExist4Chat(chatId)) {
                executeSendMessage(sendService.sendWarning(chatId, update.getMessage()));
            } else {
                executeSendMessage(sendService.sendIsEmployeeChoose(chatId, update.getMessage()));
            }
        }, "/настроить", "/setup");

        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            boolean checked = service.checkSettings(chatId);
            if (checked)
                sendService.scheduleToday(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            else
                executeSendMessage(sendService.sendNotConfigured(chatId));
        }, "/сегодня", "/today");

        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            boolean checked = service.checkSettings(chatId);
            if (checked)
                sendService.scheduleTomorrow(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            else
                executeSendMessage(sendService.sendNotConfigured(chatId));
        }, "/завтра", "/tomorrow");

        registerCommands(update -> {
            long chatId = update.getMessage().getChatId();
            boolean checked = service.checkSettings(chatId);
            if (checked)
                sendService.sendSchedule(chatId, service.getSettings(chatId), new Date()).thenAccept(this::executeSendMessage);
            else
                executeSendMessage(sendService.sendNotConfigured(chatId));
        }, "/расписание", "/schedule");

        registerCommand("/pairschedule", update -> {
            long chatId = update.getMessage().getChatId();
            executeSendMessage(sendService.sendPairSchedule(chatId));
        });

        registerCommand("/start", update -> {
            long chatId = update.getMessage().getChatId();
            executeSendMessage(sendService.sendStart(chatId));
        });
    }

    private void registerCallbackQueryConsumers() {
        registerCallbackQueryConsumer("warning", query -> {
            Message message = query.update().getCallbackQuery().getMessage();
            service.createSettings(message.getChat());
            clearKeyboard(message);
            Message setupMessage = new Message();
            setupMessage.setMessageId(Integer.valueOf(query.data()));
            executeSendMessage(sendService.sendIsEmployeeChoose(message.getChatId(), setupMessage));
        });

        registerCallbackQueryConsumer("studentTrue", query -> {
            Message message = query.update().getCallbackQuery().getMessage();
            clearKeyboard(message);
            sendService.sendBranches(message.getChatId()).thenAccept(this::executeSendMessage);
            Settings settings = service.getSettings(message.getChatId());
            settings.setEmployee(false);
            service.saveSettings(settings);
        });

        registerCallbackQueryConsumer("schedule", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            Message message = query.update().getCallbackQuery().getMessage();
            LocalDate localDate = LocalDate.parse(query.data());
            EditMessageText editMessageText = new EditMessageText();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setChatId(chatId);
            editMessageReplyMarkup.setMessageId(message.getMessageId());
            editMessageText.setMessageId(message.getMessageId());
            editMessageText.setChatId(chatId);
            Date date = new Date();
            date.setYear(localDate.getYear() - 1900);
            date.setMonth(localDate.getMonthValue());
            date.setDate(localDate.getDayOfMonth());
            sendService.sendSchedule(chatId, service.getSettings(chatId), date).thenAccept(sendMessage -> {
                editMessageText.setText(sendMessage.getText());
                editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup());

                try {
                    execute(editMessageText);
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    executeSendMessage(sendService.sendException(chatId, e));
                }
            });
        });

        registerCallbackQueryConsumer("employeeTrue", query -> {
            Message message = query.update().getCallbackQuery().getMessage();
            clearKeyboard(message);
            sendService.sendBranches(message.getChatId()).thenAccept(this::executeSendMessage);
            Settings settings = service.getSettings(message.getChatId());
            settings.setEmployee(true);
            service.saveSettings(settings);
        });

        registerCallbackQueryConsumer("employee", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            clearKeyboard(query.update().getCallbackQuery().getMessage());
            parserService.getEmployees(service.getSettings(chatId).getBranch()).thenApply(listScheduleParserResult -> {
                try {
                    return listScheduleParserResult.get();
                } catch (ScheduleParserException e) {
                    executeSendMessage(sendService.sendException(chatId, e));
                    throw new RuntimeException();
                }
            }).thenAccept(employees -> {
                Employee employee = employees.stream().filter(employee1 -> employee1.value().equals(query.data())).findFirst().orElseThrow();
                service.setEmployee(chatId, employee);
                executeSendMessage(sendService.sendSettings(chatId, service.getSettings(chatId)));
            });
        });

        registerCallbackQueryConsumer("branch", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            clearKeyboard(query.update().getCallbackQuery().getMessage());
            parserService.getBranches().thenApply(listScheduleParserResult -> {
                try {
                    return listScheduleParserResult.get();
                } catch (ScheduleParserException e) {
                    executeSendMessage(sendService.sendException(chatId, e));
                    throw new RuntimeException();
                }
            }).thenAccept(branches -> {
                Branch branch = branches.stream().filter(branch1 -> branch1.value().equals(query.data())).findFirst().orElseThrow();
                service.setBranch(chatId, branch);
                if (service.getSettings(chatId).isEmployee())
                    createEmployeeKeyboard(chatId, 0).thenAccept(keyboardMarkup ->
                            executeSendMessage(sendService.sendEmployee(chatId, keyboardMarkup)));
                else
                    sendService.sendKits(chatId, service.getSettings(chatId)).thenAccept(this::executeSendMessage);
            });
        });

        registerCallbackQueryConsumer("kit", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            parserService.getKits(service.getSettings(chatId).getBranch())
                    .thenApply(listScheduleParserResult -> {
                        try {
                            return listScheduleParserResult.get();
                        } catch (ScheduleParserException e) {
                            executeSendMessage(sendService.sendException(chatId, e));
                            throw new RuntimeException();
                        }
            }).thenAccept(kits -> {
                Kit kit = kits.stream().filter(kit1 -> kit1.value().equals(query.data())).findFirst().orElseThrow();

                //todo подумать над оптимизацией - скачивать список групп слишком жирно
                parserService.getGroups(service.getSettings(chatId).getBranch(), kit.value()).thenApply(listScheduleParserResult -> {
                    try {
                        return listScheduleParserResult.get();
                    } catch (ScheduleParserException e) {
                        executeSendMessage(sendService.sendException(chatId, e));
                        throw new RuntimeException();
                    }
                }).thenAccept(groups -> {
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
            parserService.getGroups(
                    service.getSettings(chatId).getBranch(),
                    service.getSettings(chatId).getKit()
            ).thenApply(listScheduleParserResult -> {
                try {
                    return listScheduleParserResult.get();
                } catch (ScheduleParserException e) {
                    executeSendMessage(sendService.sendException(chatId, e));
                    throw new RuntimeException();
                }
            }).thenAccept(groups -> {
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

        //todo лол а посути можно сделать это всё за одну команду
        registerCallbackQueryConsumer("employeenext", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(query.update().getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setChatId(chatId);
            createEmployeeKeyboard(chatId, Integer.parseInt(query.data()) + 1).thenAccept(inlineKeyboardMarkup -> {
                editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        registerCallbackQueryConsumer("employeeprev", query -> {
            long chatId = query.update().getCallbackQuery().getMessage().getChatId();
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
            editMessageReplyMarkup.setMessageId(query.update().getCallbackQuery().getMessage().getMessageId());
            editMessageReplyMarkup.setChatId(chatId);
            createEmployeeKeyboard(chatId, Integer.parseInt(query.data()) - 1).thenAccept(inlineKeyboardMarkup -> {
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
            statisticService.add(CommandUsageStatistic.builder()
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
        callbackQueryService.putCallbackQueryConsumer(command, callbackQuery -> {
            Chat chat = callbackQuery.update().getCallbackQuery().getMessage().getChat();
            statisticService.add(CallbackUsageStatistic.builder()
                    .chatFirstName(chat.getFirstName())
                    .chatLastName(chat.getLastName())
                    .chatUsername(chat.getUserName())
                    .chatTitle(chat.getTitle())
                    .callback(command)
                    .data(callbackQuery.data())
                    .date(new Date())
                    .setSettings(service.getSettings(chat.getId()))
                    .userUsername(callbackQuery.update().getCallbackQuery().getMessage().getFrom().getUserName())
                    .build());
            action.accept(callbackQuery);
        });
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
        return parserService.getGroups(
                service.getSettings(chatId).getBranch(),
                service.getSettings(chatId).getKit()
        ).thenApply(listScheduleParserResult -> {
            try {
                return listScheduleParserResult.get();
            } catch (ScheduleParserException e) {
                executeSendMessage(sendService.sendException(chatId, e));
                throw new RuntimeException();
            }
        }).thenApply(groups -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            List<InlineKeyboardButton> buttons1 = new LinkedList<>();
            if (page != 0)
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("предыдущее")
                                .callbackData("groupprev " + page)
                                .build()
                );
            for (int i = page * 14; i < Math.min(page * 14 + 14, groups.size()); i++) {
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

    private CompletableFuture<InlineKeyboardMarkup> createEmployeeKeyboard(long chatId, int page) {
        return parserService.getEmployees(
                service.getSettings(chatId).getBranch()
        ).thenApply(listScheduleParserResult -> {
            try {
                return listScheduleParserResult.get();
            } catch (ScheduleParserException e) {
                executeSendMessage(sendService.sendException(chatId, e));
                throw new RuntimeException();
            }
        }).thenApply(employees -> {
            List<List<InlineKeyboardButton>> buttons = new LinkedList<>();
            List<InlineKeyboardButton> buttons1 = new LinkedList<>();
            if (page != 0)
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("предыдущее")
                                .callbackData("employeeprev " + page)
                                .build()
                );
            for (int i = page * 14; i < Math.min(page * 14 + 14, employees.size()); i++) {
                Employee employee = employees.get(i);
                buttons1.add(InlineKeyboardButton.builder()
                        .text(employee.title())
                        .callbackData("employee " + employee.value())
                        .build());

                if (buttons1.size() >= 2) {
                    buttons.add(buttons1);
                    buttons1 = new LinkedList<>();
                }
            }
            if ((page * 14 + 14) < employees.size()) {
                buttons1.add(
                        InlineKeyboardButton.builder()
                                .text("следующее")
                                .callbackData("employeenext " + page)
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