package com.github.javakira.api;

import java.time.LocalDate;
import java.util.List;

public record Card(LocalDate date, List<Pair> pairList) {
    public Card(LocalDate date, List<Pair> pairList) {
        this.date = date;
        this.pairList = pairList;
    }
}
