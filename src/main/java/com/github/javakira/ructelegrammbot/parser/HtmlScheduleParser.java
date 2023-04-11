package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.Card;

import com.github.javakira.ructelegrammbot.model.Cards;
import com.github.javakira.ructelegrammbot.model.Pair;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HtmlScheduleParser implements ScheduleParser {
    private final ExecutorService executor
            = Executors.newFixedThreadPool(2);

    @Override
    public CompletableFuture<Cards> getGroupCards(String branch, String kit, String group) {
        return CompletableFuture.supplyAsync(() -> {
            List<Card> cards = new ArrayList<>();
            try {
                Connection connection = Jsoup.connect(link);
                HashMap<String, String> data = new HashMap<>();
                data.put("branch", branch);
                data.put("year", kit);
                data.put("group", group);
                connection.data(data);
                Document document = connection.post();

                Elements cardElements = document.getElementsByClass("card");
                for (Element cardElement : cardElements) {
                    List<Pair> pairList = new ArrayList<>();
                    if (cardElements.size() <= 1)
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
                    cards.add(new Card(parser.parse(strDate), pairList));
                }
            } catch (Exception e) {
                HashMap<String, String> data = new HashMap<>();
                data.put("branch", branch);
                data.put("year", kit);
                data.put("group", group);
                log.error(data + " : " + e);
            }

            return new Cards(cards);
        }, executor);
    }

    @Override
    public CompletableFuture<Cards> getEmployeeCards(String branch, String employee) {
        return CompletableFuture.supplyAsync(() -> {
            List<Card> cardList = new ArrayList<>();
            try {
                Connection connection = Jsoup.connect(employeeLink);
                HashMap<String, String> data = new HashMap<>();
                data.put("branch", branch);
                data.put("employee", employee);
                connection.data(data);
                Document document = connection.post();

                Elements cards = document.getElementsByClass("card");
                for (Element cardElement : cards) {
                    List<Pair> pairList = new ArrayList<>();
                    if (cards.size() <= 1)
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
                    cardList.add(new Card(parser.parse(strDate), pairList));
                }
            } catch (Exception e) {
                HashMap<String, String> data = new HashMap<>();
                data.put("branch", branch);
                data.put("employee", employee);
                log.error(data + " : " + e);
            }

            return new Cards(cardList);
        });
    }
}
