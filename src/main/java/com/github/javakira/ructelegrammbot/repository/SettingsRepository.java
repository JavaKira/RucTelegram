package com.github.javakira.ructelegrammbot.repository;

import com.github.javakira.ructelegrammbot.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
}
