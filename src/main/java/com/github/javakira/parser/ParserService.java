package com.github.javakira.parser;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ParserService {
    private final ExecutorService executor
            = Executors.newFixedThreadPool(50);

    private final ScheduleParser parser = new HtmlScheduleParser();

    public CompletableFuture<List<Branch>> branches() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseBranches();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<List<Kit>> kits(Branch branch) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseKits(branch.value());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<List<Group>> groups(Branch branch, Kit kit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseGroups(branch.value(), kit.value());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<List<Employee>> employee(Branch branch) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseEmployees(branch.value());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Cards> groupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull LocalDate searchDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseGroupCards(branch, kit, group, searchDate);
            } catch (Exception e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<Cards> employeeCards(@NonNull String branch, @NonNull String employee, @NonNull LocalDate searchDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parser.parseEmployeeCards(branch, employee, searchDate);
            } catch (Exception e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
