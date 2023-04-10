package com.github.javakira.ructelegrammbot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public record Card(Date date, List<Pair> pairList) {
    public Card(Date date, List<Pair> pairList) {
        this.date = date;
        this.pairList = pairList;
    }
}
