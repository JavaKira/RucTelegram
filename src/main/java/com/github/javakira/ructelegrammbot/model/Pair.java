package com.github.javakira.ructelegrammbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair {
    int index;
    String name, by, place, type, group;
}
