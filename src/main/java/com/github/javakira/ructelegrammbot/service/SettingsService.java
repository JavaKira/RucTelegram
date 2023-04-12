package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return getAll().stream().filter(settings -> settings.getChatId() == chatId).findAny().orElseThrow();
    }

    public void saveSettings(Settings settings) {
        repository.save(settings);
    }

    public void createSettings(long chatId) {
        repository.save(new Settings(chatId));
    }

    public void setBranch(long chatId, String argument) {
        Settings settings = getSettings(chatId);
        settings.setBranch(argument);
        saveSettings(settings);
    }

    public void setEmployee(long chatId, String argument) {
        Settings settings = getSettings(chatId);
        settings.setEmployeeKey(argument);
        saveSettings(settings);
    }

    public void setKit(long chatId, String argument) {
        Settings settings = getSettings(chatId);
        settings.setKit(argument);
        saveSettings(settings);
    }

    public void setGroup(long chatId, String argument) {
        Settings settings = getSettings(chatId);
        settings.setGroupKey(argument);
        saveSettings(settings);
    }
}
