package com.github.javakira.parser;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class ScheduleRequest {
    private String branch;
    private String employee;
    private String kit;
    private String group;

    private boolean searchDate;
    private LocalDate schedulerDate;
    private LocalDate dateSearch;

    public Map<String, String> data(boolean isEmployee) {
        Map<String, String> result = new HashMap<>();

        if (isEmployee) {
            if (branch != null)
                result.put("branch", branch);

            if (employee != null)
                result.put("employee", employee);
        } else {
            if (branch != null)
                result.put("branch", branch);

            if (kit != null)
                result.put("year", kit);

            if (group != null)
                result.put("group", group);
        }

        if (searchDate) {
            result.put("search-date", "search-date");
            result.put("scheduler-date", format(schedulerDate));
            result.put("date-search", format(dateSearch));
        }

        return result;
    }

    private String format(LocalDate date) {
        int month = date.plusMonths(1).getMonth().getValue();
        return
                (1900 + date.getYear()) + "-" +
                        (month < 10 ? "0" + month : month) + "-" +
                        (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth());
    }
}
