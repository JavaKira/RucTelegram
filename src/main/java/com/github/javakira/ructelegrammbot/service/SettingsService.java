package com.github.javakira.ructelegrammbot.service;

import com.github.javakira.ructelegrammbot.model.Settings;
import com.github.javakira.ructelegrammbot.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {
    private final SettingsRepository repository;

    public SettingsService(SettingsRepository repository) {
        this.repository = repository;
    }

    public List<Settings> getAll() {
        return repository.findAll();
    }

    public boolean isSettingsExist4Chat(long chatId) {
        return getAll().stream().anyMatch(settings -> settings.getChatId() == chatId);
    }

    public void createSettings(long chatId) {
        repository.save(new Settings(chatId));
    }
}
