package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    CompletableFuture<List<Branch>> getBranches();
    CompletableFuture<List<Kit>> getKits(String branch);
    CompletableFuture<List<Group>> getGroups(String branch, String kit);
    CompletableFuture<List<Employee>> getEmployees(String branch);
    CompletableFuture<Cards> getGroupCards(String branch, String kit, String group);
    CompletableFuture<Cards> getEmployeeCards(String branch, String employee);
}