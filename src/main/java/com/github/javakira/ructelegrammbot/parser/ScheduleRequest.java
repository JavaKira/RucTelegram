package com.github.javakira.ructelegrammbot.parser;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.javakira.ructelegrammbot.parser.ScheduleParser.employeeLink;
import static com.github.javakira.ructelegrammbot.parser.ScheduleParser.link;

@Data
@Builder
@Slf4j
class ScheduleRequest {
    private String branch;
    private String employee;
    private String kit;
    private String group;

    private boolean searchDate;
    private Date schedulerDate;
    private Date dateSearch;

    private Map<String, String> data(boolean isEmployee) {
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

    private String format(Date date) {
        int month = date.getMonth() + 1;
        return
                (1900 + date.getYear()) + "-" +
                (month < 10 ? "0" + month : month) + "-" +
                (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
    }

    Document document() throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(link);
        connection.data(data(false));

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }

    Document documentEmployee() throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(employeeLink);
        connection.data(data(true));

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }
}
