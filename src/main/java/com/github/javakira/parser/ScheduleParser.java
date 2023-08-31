package com.github.javakira.parser;

import lombok.NonNull;
import org.jsoup.nodes.Document;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    List<Branch> parseBranches() throws Exception;

    List<Kit> parseKits(@NonNull String branch) throws Exception;

    List<Group> parseGroups(@NonNull String branch, @NonNull String kit) throws Exception;

    List<Employee> parseEmployees(@NonNull String branch) throws Exception;

    Cards parseGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull LocalDate searchDate) throws Exception;

    Cards parseEmployeeCards(@NonNull String branch, @NonNull String employee, @NonNull LocalDate searchDate) throws Exception;

    Document document(ScheduleRequest request) throws ServerNotRespondingException;

    Document employeeDocument(ScheduleRequest request) throws ServerNotRespondingException;
}