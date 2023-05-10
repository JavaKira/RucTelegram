package com.github.javakira.ructelegrammbot.parser;

public class ScheduleParserResult<T> {
    private T value;
    private ScheduleParserException exception;

    public ScheduleParserResult(T value) {
        this.value = value;
    }

    public ScheduleParserResult(ScheduleParserException exception) {
        this.exception = exception;
    }

    public T get() throws ScheduleParserException {
        if (exception != null)
            throw exception;

        return value;
    }

    @Override
    public String toString() {
        return "ScheduleParserResult{" +
                "value=" + value +
                ", exception=" + exception +
                '}';
    }
}
