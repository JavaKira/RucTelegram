package com.github.javakira.ructelegrammbot.model.statistic;

import com.github.javakira.ructelegrammbot.model.Settings;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity(name = "tg_stat_command")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandUsageStatistic {
    @Id
    @GeneratedValue
    public long id;

    public String command;
    public Date date;

    //Chat data //ToDo можно вынести 4 нижних поля в 1 класс
    public String chatUsername;
    public String chatLastName;
    public String chatFirstName;
    public String chatTitle;

    //Chat settings data
    public String groupTitle;
    public String branchTitle;
    public String kitTitle;
    public String employeeTitle;

    /**
     *username of user what use command
     */
    public String userUsername;

    public static class CommandUsageStatisticBuilder {
        public CommandUsageStatisticBuilder setSettings(Settings settings) {
            if (settings != null) {
                this.groupTitle = settings.getGroupTitle();
                this.branchTitle = settings.getBranchTitle();
                this.kitTitle = settings.getKitTitle();
                this.employeeTitle = settings.getEmployeeTitle();
            }

            return this;
        }
    }
}
