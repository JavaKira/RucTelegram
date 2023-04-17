package com.github.javakira.ructelegrammbot.repository;

import com.github.javakira.ructelegrammbot.model.statistic.CommandUsageStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandUsageStatisticRepository extends JpaRepository<CommandUsageStatistic, Long> {
}
