package com.github.javakira.ructelegrammbot.statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandUsageStatisticRepository extends JpaRepository<CommandUsageStatistic, Long> {
}
