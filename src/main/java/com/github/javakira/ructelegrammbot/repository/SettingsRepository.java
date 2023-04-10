package com.github.javakira.ructelegrammbot.repository;

import com.github.javakira.ructelegrammbot.model.Settings;
import org.springframework.data.repository.CrudRepository;

public interface SettingsRepository extends CrudRepository<Settings, Long> {
}
