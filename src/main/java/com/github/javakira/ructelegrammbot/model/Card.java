package com.github.javakira.ructelegrammbot.model;

import java.util.Date;
import java.util.List;

public record Card(Date date, List<Pair> pairList) {
    public Card(Date date, List<Pair> pairList) {
        this.date = date;
        this.pairList = pairList;
    }
}
