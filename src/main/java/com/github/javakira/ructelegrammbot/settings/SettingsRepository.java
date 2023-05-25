package com.github.javakira.ructelegrammbot.settings;

import com.github.javakira.ructelegrammbot.settings.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
}
