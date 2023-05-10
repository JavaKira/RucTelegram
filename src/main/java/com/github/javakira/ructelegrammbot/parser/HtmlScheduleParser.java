package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HtmlScheduleParser implements ScheduleParser {
    private static HtmlScheduleParser instance;

    private final ExecutorService executor
            = Executors.newFixedThreadPool(2);

    public static ScheduleParser instance() {
        if (instance == null)
            instance = new HtmlScheduleParser();

        return instance;
    }

    private HtmlScheduleParser() {

    }

    private Document document() throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(link);

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }

    private Document document(@NonNull Map<String, String> data) throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(link);
        connection.data(data);

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }

    private Document documentEmployee(@NonNull Map<String, String> data) throws ServerNotRespondingException {
        Connection connection = Jsoup.connect(employeeLink);
        connection.data(data);

        try {
            return connection.post();
        } catch (IOException e) {
            throw new ServerNotRespondingException();
        }
    }

    @Override
    public CompletableFuture<ScheduleParserResult<List<Branch>>> getBranches() {
        return CompletableFuture.supplyAsync(() -> {
            Elements elements;

            Document document;
            try {
                document = document();
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Element employee = document.getElementsByAttribute("name").stream()
                    .filter(element -> element.attr("name").equals("branch"))
                    .toList().get(0);
            elements = employee.children();
            elements.remove(0);

            List<Branch> branchList = new ArrayList<>();
            elements.forEach(element -> branchList.add(new Branch(element.text(), element.attr("value"))));
            return new ScheduleParserResult<>(branchList);
        }, executor);
    }

    @Override
    public CompletableFuture<ScheduleParserResult<List<Kit>>> getKits(String branch) {
        return CompletableFuture.supplyAsync(() -> {
            Elements elements;

            HashMap<String, String> data = new HashMap<>();
            data.put("branch", branch);
            Document document;
            try {
                document = document(data);
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Element employee = document.getElementsByAttribute("name").stream()
                    .filter(element -> element.attr("name").equals("year"))
                    .toList().get(0);
            elements = employee.children();
            elements.remove(0);

            List<Kit> kits = new ArrayList<>();
            elements.forEach(element -> kits.add(new Kit(element.text(), element.attr("value"))));
            return new ScheduleParserResult<>(kits);
        }, executor);
    }

    @Override
    public CompletableFuture<ScheduleParserResult<List<Group>>> getGroups(String branch, String kit) {
        return CompletableFuture.supplyAsync(() -> {
            Elements elements;
            HashMap<String, String> data = new HashMap<>();
            data.put("branch", branch);
            data.put("year", kit);
            Document document;
            try {
                document = document(data);
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Element employee = document.getElementsByAttribute("name").stream()
                    .filter(element -> element.attr("name").equals("group"))
                    .toList().get(0);
            elements = employee.children();
            elements.remove(0);

            List<Group> groups = new ArrayList<>();
            elements.forEach(element -> groups.add(new Group(element.text(), element.attr("value"))));
            return new ScheduleParserResult<>(groups);
        }, executor);
    }

    @Override
    public CompletableFuture<ScheduleParserResult<List<Employee>>> getEmployees(String branch) {
        return CompletableFuture.supplyAsync(() -> {
            Elements elements;
            HashMap<String, String> data = new HashMap<>();
            data.put("branch", branch);
            Document document;
            try {
                document = documentEmployee(data);
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Element employee = document.getElementsByAttribute("name").stream()
                    .filter(element -> element.attr("name").equals("employee"))
                    .toList().get(0);
            elements = employee.children();
            elements.remove(0);

            List<Employee> employees = new ArrayList<>();
            elements.forEach(element -> employees.add(new Employee(element.text(), element.attr("value"))));
            return new ScheduleParserResult<>(employees);
        }, executor);
    }

    @Override
    public CompletableFuture<ScheduleParserResult<Cards>> getGroupCards(String branch, String kit, String group) {
        return CompletableFuture.supplyAsync(() -> {
            List<Card> cards = new ArrayList<>();
            HashMap<String, String> data = new HashMap<>();
            data.put("branch", branch);
            data.put("year", kit);
            data.put("group", group);
            Document document;
            try {
                document = document(data);
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Elements cardElements = document.getElementsByClass("card");
            for (Element cardElement : cardElements) {
                List<Pair> pairList = new ArrayList<>();
                if (cardElements.size() == 0)
                    return new ScheduleParserResult<>(new Cards(cards));

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

            return new ScheduleParserResult<>(new Cards(cards));
        }, executor);
    }

    @Override
    public CompletableFuture<ScheduleParserResult<Cards>> getEmployeeCards(String branch, String employee) {
        return CompletableFuture.supplyAsync(() -> {
            List<Card> cardList = new ArrayList<>();
            HashMap<String, String> data = new HashMap<>();
            data.put("branch", branch);
            data.put("employee", employee);
            Document document;
            try {
                document = documentEmployee(data);
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            }

            Elements cards = document.getElementsByClass("card");
            for (Element cardElement : cards) {
                List<Pair> pairList = new ArrayList<>();
                if (cards.size() == 0)
                    return new ScheduleParserResult<>(new Cards(cardList));

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

            return new ScheduleParserResult<>(new Cards(cardList));
        });
    }
}
