package com.github.javakira.ructelegrammbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Cards {
    private List<Card> list;

    public Optional<Card> getToday() {
        Date currentDate = new Date();
        return list.stream()
                .filter(card -> card.date().compareTo(new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate())) == 0)
                .findAny();
    }

    public Optional<Card> getTomorrow() {
        Date currentDate = new Date();
        return list.stream()
                .filter(card -> card.date().compareTo(new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate() + 1)) == 0)
                .findAny();
    }
}
