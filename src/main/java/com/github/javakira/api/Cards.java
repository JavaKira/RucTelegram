package com.github.javakira.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Cards {
    private List<Card> list;

    public Optional<Card> today() {
        LocalDate currentDate = LocalDate.now();
        return list.stream()
                .filter(card -> card.date().isEqual(currentDate))
                .findAny();
    }

    public Optional<Card> tomorrow() {
        LocalDate currentDate = LocalDate.now();
        return list.stream()
                .filter(card -> card.date().isEqual(currentDate.plusDays(1)))
                .findAny();
    }

    public List<Card> week() {
        return list;
    }
}
