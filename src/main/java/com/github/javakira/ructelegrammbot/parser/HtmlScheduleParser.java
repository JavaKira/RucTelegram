package com.github.javakira.ructelegrammbot.parser;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class HtmlScheduleParser implements ScheduleParser {
    public List<Branch> parseBranches() throws Exception {
        Elements elements;
        Document document = ScheduleRequest.builder().build().document();
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("branch"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Branch> branchList = new ArrayList<>();
        elements.forEach(element -> branchList.add(new Branch(element.text(), element.attr("value"))));
        return branchList;
    }

    public List<Kit> parseKits(@NonNull String branch) throws Exception {
        Elements elements;
        Document document = ScheduleRequest.builder()
                .branch(branch)
                .build()
                .document();
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("year"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Kit> kits = new ArrayList<>();
        elements.forEach(element -> kits.add(new Kit(element.text(), element.attr("value"))));
        return kits;
    }

    public List<Group> parseGroups(@NonNull String branch, @NonNull String kit) throws Exception {
        Elements elements;
        Document document = ScheduleRequest.builder()
                .branch(branch)
                .kit(kit)
                .build()
                .document();
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("group"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Group> groups = new ArrayList<>();
        elements.forEach(element -> groups.add(new Group(element.text(), element.attr("value"))));
        return groups;
    }

    public List<Employee> parseEmployees(@NonNull String branch) throws Exception {
        Elements elements;
        Document document = ScheduleRequest.builder()
                .branch(branch)
                .build()
                .documentEmployee();
        Element employee = document.getElementsByAttribute("name").stream()
                .filter(element -> element.attr("name").equals("employee"))
                .toList().get(0);
        elements = employee.children();
        elements.remove(0);
        List<Employee> employees = new ArrayList<>();
        elements.forEach(element -> employees.add(new Employee(element.text(), element.attr("value"))));
        return employees;
    }

    public Cards parseGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull Date searchDate) throws Exception {
        List<Card> cards = new ArrayList<>();
        Document document = ScheduleRequest.builder()
                .branch(branch)
                .kit(kit)
                .group(group)
                .searchDate(true)
                .dateSearch(searchDate)
                .schedulerDate(new Date())
                .build()
                .document();
        Elements cardElements = document.getElementsByClass("card");
        for (Element cardElement : cardElements) {
            List<Pair> pairList = new ArrayList<>();
            if (cardElements.size() == 0)
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
                        pairIndex - 1,
                        pairName.trim(),
                        split[1].trim(),
                        split1[0].trim(),
                        split1[1].trim(),
                        split[1].trim()
                ));
            }

            String strDate = header.text().split(" ")[0];
            SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
            try {
                cards.add(new Card(parser.parse(strDate), pairList));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return new Cards(cards);

    }

    public Cards parseEmployeeCards(@NonNull String branch, @NonNull String employee) throws Exception {
        List<Card> cardList = new ArrayList<>();
        Document document = ScheduleRequest.builder()
                .branch(branch)
                .employee(employee)
                .build()
                .documentEmployee();
        Elements cards = document.getElementsByClass("card");
        for (Element cardElement : cards) {
            List<Pair> pairList = new ArrayList<>();
            if (cards.size() == 0)
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
                        pairIndex - 1,
                        pairName.trim(),
                        split[1].trim(),
                        split1[0].trim(),
                        split1[1].trim(),
                        split[1].trim()
                ));
            }

            String strDate = header.text().split(" ")[0];
            SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
            try {
                cardList.add(new Card(parser.parse(strDate), pairList));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return new Cards(cardList);
    }
}
