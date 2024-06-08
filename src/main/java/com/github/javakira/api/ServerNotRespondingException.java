package com.github.javakira.api;

public class ServerNotRespondingException extends ScheduleParserException {
    public ServerNotRespondingException() {
        super();
    }

    public ServerNotRespondingException(Throwable cause) {
        super(cause);
    }
}
