package com.github.javakira.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleApiService {
    private static final String apiHost = "http://localhost:8080";

    public CompletableFuture<List<Branch>> branches() {
        Mono<List<Branch>> branches = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/branches")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Branch>>() {});
        return branches.toFuture();
    }

    public CompletableFuture<List<Kit>> kits(Branch branch) {
        Mono<List<Kit>> kits = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/kits?branch=" + branch.value())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Kit>>() {});
        return kits.toFuture();
    }

    public CompletableFuture<List<Group>> groups(Branch branch, Kit kit) {
        Mono<List<Group>> groups = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/groups?branch=" + branch.value() + "&kit=" + kit.value())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Group>>() {});
        return groups.toFuture();
    }

    public CompletableFuture<List<Employee>> employee(Branch branch) {
        Mono<List<Employee>> employees = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/employees?branch=" + branch.value())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Employee>>() {});
        return employees.toFuture();
    }

    public CompletableFuture<Cards> groupCards(@NonNull String branch, @NonNull String kit, @NonNull String group, @NonNull LocalDate searchDate) {
        Mono<String> employees = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/student/week?date=" + searchDate + "&branch=" + branch + "&kit=" + kit + "&group=" + group)
                .retrieve()
                .bodyToMono(String.class);
        return parseJson2Cards(employees.toFuture());
    }

    public CompletableFuture<Cards> employeeCards(@NonNull String branch, @NonNull String employee, @NonNull LocalDate searchDate) {
        Mono<String> employees = WebClient.create(apiHost)
                .get()
                .uri("/api/v1/employee/week?date=" + searchDate + "&branch=" + branch + "&employee=" + employee)
                .retrieve()
                .bodyToMono(String.class);
        return parseJson2Cards(employees.toFuture());
    }

    private CompletableFuture<Cards> parseJson2Cards(CompletableFuture<String> future) {
        return future.thenApply(str -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readTree(str);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).thenApply(json -> {
            LinkedList<Card> cardLinkedList = new LinkedList<>();
            for (JsonNode node : json.get("list")) {
                LinkedList<Pair> pairList = new LinkedList<>();
                for (JsonNode pairNode : node.get("pairList")) {
                    pairList.add(new Pair(
                            pairNode.get("index").asInt(),
                            pairNode.get("name").asText(),
                            pairNode.get("by").asText(),
                            pairNode.get("place").asText(),
                            pairNode.get("type").asText()
                    ));
                }

                Card card = new Card(LocalDate.parse(node.get("date").asText()), pairList);
                cardLinkedList.add(card);
            }

            return new Cards(cardLinkedList);
        });
    }
}
