package com.github.javakira.context;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChatContext {
    @Id
    @GeneratedValue
    private Long id;

    private long chatId;
    private ReplyState replyState = ReplyState.def;
    private int lastReplyPage;
    private boolean isEmployee;
    private String branchValue;
    private String employeeValue;
    private String kitValue;
    private String groupValue;
    private String branchTitle;
    private String employeeTitle;
    private String kitTitle;
    private String groupTitle;
}
