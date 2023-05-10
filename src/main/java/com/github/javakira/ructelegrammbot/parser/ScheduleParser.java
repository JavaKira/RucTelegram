package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    CompletableFuture<ScheduleParserResult<List<Branch>>> getBranches();
    CompletableFuture<ScheduleParserResult<List<Kit>>> getKits(String branch);
    CompletableFuture<ScheduleParserResult<List<Group>>> getGroups(String branch, String kit);
    CompletableFuture<ScheduleParserResult<List<Employee>>> getEmployees(String branch);
    CompletableFuture<ScheduleParserResult<Cards>> getGroupCards(String branch, String kit, String group);
    CompletableFuture<ScheduleParserResult<Cards>> getEmployeeCards(String branch, String employee);
}