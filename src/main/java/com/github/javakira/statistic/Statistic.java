package com.github.javakira.statistic;

import com.github.javakira.context.ChatContext;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@Builder
@Entity
public class Statistic {
    //types
    public static final String commandUsageType = "command";
    public static final String callbackUsageType = "callback";

    @Id
    @GeneratedValue
    private Long id;

    private String data;
    private LocalDate date;
    private String type;

    //Chat data
    public String chatUsername;
    public String chatLastName;
    public String chatFirstName;
    public String chatTitle;

    //Chat settings data
    public String groupTitle;
    public String branchTitle;
    public String kitTitle;
    public String employeeTitle;

    public String username;

//    public static class StatisticBuilder {
//        public StatisticBuilder setContext(ChatContext context) {
//            if (context != null) {
//                this.groupTitle = context.getGroupTitle();
//                this.branchTitle = context.getBranchTitle();
//                this.kitTitle = context.getKitTitle();
//                this.employeeTitle = context.getEmployeeTitle();
//            }
//
//            return this;
//        }
//    }
}
