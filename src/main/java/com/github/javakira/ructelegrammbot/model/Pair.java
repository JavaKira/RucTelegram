package com.github.javakira.ructelegrammbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair {
    int index;
    String name, by, place, type, group;

    public static String getTimeByIndex(int index) {
        return switch (index) {
            case 0 -> "8:30–9:15 9:25–10:10";
            case 1 -> "10:30–11:15 11:25–12:10";
            case 2 -> "12:30–13:15 13:25–14:10";
            case 3 -> "14:30–15:15 15:25–16:10";
            case 4 -> "16:20–17:05 17:15–18:00";
            case 5 -> "18:10–19:40";
            case 6 -> "19:50–21:20";
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }
}
