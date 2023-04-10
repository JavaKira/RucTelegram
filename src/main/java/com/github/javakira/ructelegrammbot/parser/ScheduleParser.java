package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.Card;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    CompletableFuture<List<Card>> getGroupCards(String branch, String kit, String group);
    CompletableFuture<List<Card>> getEmployeeCards(String branch, String employee);
}