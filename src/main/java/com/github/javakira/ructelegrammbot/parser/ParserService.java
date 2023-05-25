package com.github.javakira.ructelegrammbot.parser;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ParserService {
    private final ExecutorService executor
            = Executors.newFixedThreadPool(50);
    private final ScheduleParser parser = new HtmlScheduleParser();

    public ParserService() {
    }

    public CompletableFuture<ScheduleParserResult<List<Branch>>> getBranches() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseBranches());
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }

    public CompletableFuture<ScheduleParserResult<List<Kit>>> getKits(@NonNull String branch) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseKits(branch));
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }

    public CompletableFuture<ScheduleParserResult<List<Group>>> getGroups(@NonNull String branch, @NonNull String kit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseGroups(branch, kit));
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }

    public CompletableFuture<ScheduleParserResult<List<Employee>>> getEmployees(@NonNull String branch) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseEmployees(branch));
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }

    public CompletableFuture<ScheduleParserResult<Cards>> getGroupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull Date searchDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseGroupCards(branch, kit, group, searchDate));
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }

    public CompletableFuture<ScheduleParserResult<Cards>> getEmployeeCards(@NonNull String branch, @NonNull String employee) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new ScheduleParserResult<>(parser.parseEmployeeCards(branch, employee));
            } catch (ServerNotRespondingException e) {
                return new ScheduleParserResult<>(e);
            } catch (Exception e) {
                return new ScheduleParserResult<>(new ScheduleParserException(e));
            }
        }, executor);
    }
}
