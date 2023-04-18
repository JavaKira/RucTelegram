package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.*;
import com.github.javakira.ructelegrammbot.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.List;

@Service
public class SettingsService {
    private final SettingsRepository repository;

    @Autowired
    public SettingsService(SettingsRepository repository) {
        this.repository = repository;
    }

    public List<Settings> getAll() {
        return repository.findAll();
    }

    public boolean isSettingsExist4Chat(long chatId) {
        return getAll().stream().anyMatch(settings -> settings.getChatId() == chatId);
    }

    public Settings getSettings(long chatId) {
        return getAll().stream().filter(settings -> settings.getChatId() == chatId).findAny().orElse(null);
    }

    public void saveSettings(Settings settings) {
        repository.save(settings);
    }

    public void createSettings(Chat chat) {
        Settings settings = new Settings(chat.getId());
        settings.setUsername(chat.getUserName());
        settings.setFirstName(chat.getFirstName());
        settings.setLastName(chat.getLastName());
        settings.setTitle(chat.getTitle());
        repository.save(settings);
    }

    public void setBranch(long chatId, Branch argument) {
        Settings settings = getSettings(chatId);
        settings.setBranch(argument.value());
        settings.setBranchTitle(argument.title());
        saveSettings(settings);
    }

    public void setEmployee(long chatId, Employee argument) {
        Settings settings = getSettings(chatId);
        settings.setEmployeeKey(argument.value());
        settings.setEmployeeTitle(argument.title());
        saveSettings(settings);
    }

    public void setKit(long chatId, Kit argument) {
        Settings settings = getSettings(chatId);
        settings.setKit(argument.value());
        settings.setKitTitle(argument.title());
        saveSettings(settings);
    }

    public void setGroup(long chatId, Group argument) {
        Settings settings = getSettings(chatId);
        settings.setGroupKey(argument.value());
        settings.setGroupTitle(argument.title());
        saveSettings(settings);
    }

    public boolean checkSettings(long chatId) {
        if (!isSettingsExist4Chat(chatId)) {
            return false;
        } else {
            Settings settings = getSettings(chatId);
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
}
