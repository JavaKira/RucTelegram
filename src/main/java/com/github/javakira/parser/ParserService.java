package com.github.javakira.parser;

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ParserService {
    private final ExecutorService executor
            = Executors.newFixedThreadPool(50);

    private final ScheduleParser parser = new HtmlScheduleParser();

    public CompletableFuture<List<Branch>> branches() {
        return CompletableFuture.supplyAsync(parser::parseBranches, executor);
    }

    public CompletableFuture<List<Kit>> kits(Branch branch) {
        return CompletableFuture.supplyAsync(() -> parser.parseKits(branch.value()), executor);
    }

    public CompletableFuture<List<Group>> groups(Branch branch, Kit kit) {
        return CompletableFuture.supplyAsync(() -> parser.parseGroups(branch.value(), kit.value()), executor);
    }

    public CompletableFuture<List<Employee>> employee(Branch branch) {
        return CompletableFuture.supplyAsync(() -> parser.parseEmployees(branch.value()), executor);
    }

    public CompletableFuture<Cards> groupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull LocalDate searchDate) {
        return CompletableFuture.supplyAsync(() -> parser.parseGroupCards(branch, kit, group, searchDate), executor);
    }

    public CompletableFuture<Cards> employeeCards(@NonNull String branch, @NonNull String employee, @NonNull LocalDate searchDate) {
        return CompletableFuture.supplyAsync(() -> parser.parseEmployeeCards(branch, employee, searchDate), executor);
    }
}
