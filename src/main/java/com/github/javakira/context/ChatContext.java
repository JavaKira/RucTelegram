package com.github.javakira.context;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ChatContext {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id; //todo можно заменить на chatId, но это потребует рефакторинг базы данных
    private long chatId;
    private LocalDateTime creationDate;
    private ReplyState replyState = ReplyState.def; //todo добавить @Enumurated, но это потребует рефакторинг базы данных
    private int lastReplyPage;
    private boolean isEmployee;
    private String branchValue, employeeValue,  kitValue, groupValue;
    private String branchTitle, employeeTitle, kitTitle, groupTitle;
}
