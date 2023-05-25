package com.github.javakira.ructelegrammbot.parser;

import com.github.javakira.ructelegrammbot.model.*;
import lombok.NonNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ScheduleParser {
    String link = "https://schedule.ruc.su/";
    String employeeLink = "https://schedule.ruc.su/employee/";

    List<Branch> parseBranches() throws Exception;

    List<Kit> parseKits(@NonNull String branch) throws Exception;

    List<Group> parseGroups(@NonNull String branch, @NonNull String kit) throws Exception;

    List<Employee> parseEmployees(@NonNull String branch) throws Exception;

    Cards parseGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull Date searchDate) throws Exception;

    Cards parseEmployeeCards(@NonNull String branch, @NonNull String employee) throws Exception;
}