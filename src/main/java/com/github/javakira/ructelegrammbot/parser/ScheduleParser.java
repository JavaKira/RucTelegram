package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.Card;
import com.github.javakira.ructelegrammbot.model.Cards;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    CompletableFuture<Cards> getGroupCards(String branch, String kit, String group);
    CompletableFuture<Cards> getEmployeeCards(String branch, String employee);
}