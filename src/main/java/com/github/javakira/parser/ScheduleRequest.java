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
            result.put("scheduler-date", schedulerDate.toString());
            result.put("date-search", dateSearch.toString());
        }

        return result;
    }
}
