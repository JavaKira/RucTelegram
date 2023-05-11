package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.*;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    CompletableFuture<ScheduleParserResult<List<Branch>>> getBranches();
    CompletableFuture<ScheduleParserResult<List<Kit>>> getKits(@NonNull String branch);
    CompletableFuture<ScheduleParserResult<List<Group>>> getGroups(@NonNull String branch, @NonNull String kit);
    CompletableFuture<ScheduleParserResult<List<Employee>>> getEmployees(@NonNull String branch);
    CompletableFuture<ScheduleParserResult<Cards>> getGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group);
    CompletableFuture<ScheduleParserResult<Cards>> getEmployeeCards(@NonNull String branch, @NonNull String employee);
}