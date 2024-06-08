package com.github.javakira.command;

import com.github.javakira.api.Card;
import com.github.javakira.api.Cards;
import com.github.javakira.api.Pair;
import com.github.javakira.util.Formatter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WeekTextBuilder {
    private final Cards cards;
    private final String title;
    private final boolean isWeekScheduleShorted;

    public String text() {
        StringBuilder builder = new StringBuilder();
        if (!cards.getList().isEmpty()) {
            builder.append("ℹ️ Используй инлайн-кнопки внизу сообщения, что бы выбрать нужную неделю\n\n");
            builder.append("#Расписание ")
                    .append(title)
                    .append("\n");

            for (int cardIndex = 0; cardIndex < cards.getList().size(); cardIndex++) {
                Card card = cards.getList().get(cardIndex);
                builder.append("<b>")
                        .append(card.date().getDayOfMonth())
                        .append(".")
                        .append(card.date().getMonth().getValue())
                        .append(".")
                        .append(card.date().getYear())
                        .append(" (")
                        .append(Formatter.formatDayOfWeek(card.date().getDayOfWeek()))
                        .append(")</b>\n");
                for (int i = 0; i < card.pairList().size(); i++) {
                    Pair pair = card.pairList().get(i);
                    if (isWeekScheduleShorted) {
                        builder
                                .append(pair.index())
                                .append(" — ")
                                .append(pair.name())
                                .append("\n");
                    } else {
                        builder
                                .append("<b>")
                                .append(pair.index())
                                .append(" — ")
                                .append(pair.name())
                                .append("</b>\n")
                                .append(pair.by())
                                .append("\n")
                                .append(pair.place())
                                .append("\n")
                                .append(pair.type())
                                .append("\n\n");
                    }
                }

                builder.append("\n");
            }
        } else {
            builder.append("Расписания для ").append(title).append(" на неделю нет");
        }

        return builder.toString();
    }
}
