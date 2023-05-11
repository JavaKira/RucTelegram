package com.github.javakira.ructelegrammbot.parser;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.javakira.ructelegrammbot.parser.ScheduleParser.employeeLink;
import static com.github.javakira.ructelegrammbot.parser.ScheduleParser.link;

@Data
@Builder
public class ScheduleRequest {
    private boolean isEmployee;
    private String branch;
    private String employee;
    private String kit;
    private String group;

    private Map<String, String> data() {
        Map<String, String> result = new HashMap<>();

        if (isEmployee)
            result.putAll(Map.of(
                    "branch", branch,
                    "employee", employee
            ));
        else
            result.putAll(Map.of(
                    "branch", branch,
                    "year", kit,
                    "group", group
            ));

        return result;
    }

    Document document() throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(link);
        connection.data(data());

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }

    Document documentEmployee() throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(employeeLink);
        connection.data(data());

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }
}
