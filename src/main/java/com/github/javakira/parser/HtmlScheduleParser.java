package com.github.javakira.parser;

import lombok.NonNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlScheduleParser implements ScheduleParser {
    @Override
    public List<Branch> parseBranches() throws Exception {
        Elements elements;
        Document document = document(ScheduleRequest.builder().build());
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("branch"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Branch> branchList = new ArrayList<>();
        elements.forEach(element -> branchList.add(new Branch(element.text(), element.attr("value"))));
        return branchList;
    }

    @Override
    public List<Kit> parseKits(@NonNull String branch) throws Exception {
        Elements elements;
        Document document = document(ScheduleRequest.builder()
                .branch(branch)
                .build());
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("year"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Kit> kits = new ArrayList<>();
        elements.forEach(element -> kits.add(new Kit(element.text(), element.attr("value"))));
        return kits;
    }

    @Override
    public List<Group> parseGroups(@NonNull String branch, @NonNull String kit) throws Exception {
        Elements elements;
        Document document = document(ScheduleRequest.builder()
                .branch(branch)
                .kit(kit)
                .build());
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("group"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Group> groups = new ArrayList<>();
        elements.forEach(element -> groups.add(new Group(element.text(), element.attr("value"))));
        return groups;
    }

    @Override
    public List<Employee> parseEmployees(@NonNull String branch) throws Exception {
        Elements elements;
        Document document = employeeDocument(ScheduleRequest.builder()
                .branch(branch)
                .build());
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("employee"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Employee> employees = new ArrayList<>();
        elements.forEach(element -> employees.add(new Employee(element.text(), element.attr("value"))));
        return employees;
    }

    @Override
    public Cards parseGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull LocalDate searchDate) throws Exception {
        List<Card> cards = new ArrayList<>();
        Document document = document(ScheduleRequest.builder()
                .branch(branch)
                .kit(kit)
                .group(group)
                .searchDate(true)
                .dateSearch(searchDate)
                .schedulerDate(LocalDate.now())
                .build());
        Elements cardElements = document.getElementsByClass("card");
        for (Element cardElement : cardElements) {
            List<Pair> pairList = new ArrayList<>();
            if (cardElements.isEmpty())
                return new Cards(cards);

            Elements pairs = cardElement.children();
            Element header = pairs.get(0);
            pairs.remove(0);


            for (Element element : pairs) {
                String pairName = element.children().first().text();
                String text = element.toString().replace(pairName, "").replace("Группа", "").trim();
                Matcher matcher = Pattern.compile("[0-9].").matcher(pairName);
                matcher.find();
                int pairIndex = Integer.parseInt(pairName.substring(matcher.start(), matcher.end() - 1));
                pairName = pairName.replaceAll("[0-9].", "");
                String[] split = text.split("<br>");
                String[] split1 = split[2].split(",");

                pairList.add(new Pair(
                        pairIndex,
                        pairName.trim(),
                        split[1].trim(),
                        split1[0].trim(),
                        split1[1].trim()
                ));
            }

            String strDate = header.text().split(" ")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            cards.add(new Card(LocalDate.parse(strDate, formatter), pairList));
        }

        return new Cards(cards);
    }

    @Override
    public Cards parseEmployeeCards(@NonNull String branch, @NonNull String employee, @NonNull LocalDate searchDate) throws Exception {
        List<Card> cardList = new ArrayList<>();
        Document document = employeeDocument(ScheduleRequest.builder()
                .branch(branch)
                .employee(employee)
                .searchDate(true)
                .dateSearch(searchDate)
                .schedulerDate(LocalDate.now())
                .build());
        Elements cards = document.getElementsByClass("card");
        for (Element cardElement : cards) {
            List<Pair> pairList = new ArrayList<>();
            if (cards.isEmpty())
                return new Cards(cardList);

            Elements pairs = cardElement.children();
            Element header = pairs.get(0);
            pairs.remove(0);


            for (Element element : pairs) {
                String pairName = element.children().first().text();
                String text = element.toString().replace(pairName, "").replace("Группа", "").trim();
                Matcher matcher = Pattern.compile("[0-9].").matcher(pairName);
                matcher.find();
                int pairIndex = Integer.parseInt(pairName.substring(matcher.start(), matcher.end() - 1));
                pairName = pairName.replaceAll("[0-9].", "");
                String[] split = text.split("<br>");
                String[] split1 = split[2].split(",");

                pairList.add(new Pair(
                        pairIndex,
                        pairName.trim(),
                        split[1].trim(),
                        split1[0].trim(),
                        split1[1].trim()
                ));
            }

            String strDate = header.text().split(" ")[0];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            cardList.add(new Card(LocalDate.parse(strDate, formatter), pairList));
        }

        return new Cards(cardList);
    }

    @Override
    public Document document(ScheduleRequest request) throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(link);
        connection.data(request.data(false));

        try {
            Document document = connection.post();
            System.out.println(document);
            return document;
        } catch (IOException e) {
            throw new ServerNotRespondingException(e);
        }
    }

    @Override
    public Document employeeDocument(ScheduleRequest request) throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(employeeLink);
        connection.data(request.data(true));

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException(e);
        }
    }
}
