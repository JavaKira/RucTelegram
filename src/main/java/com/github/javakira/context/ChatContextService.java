package com.github.javakira.context;

import com.github.javakira.parser.Branch;
import com.github.javakira.parser.Employee;
import com.github.javakira.parser.Group;
import com.github.javakira.parser.Kit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatContextService {
    private final ChatContextRepository repository;

    public List<ChatContext> all() {
        return repository.findAll();
    }

    public void save(ChatContext context) {
        repository.save(context);
    }

    public ChatContext get(long chatId) {
        Optional<ChatContext> optionalChatContext = all().stream().filter(settings -> settings.getChatId() == chatId).findAny();
        if (optionalChatContext.isEmpty()) {
            ChatContext newContext = new ChatContext();
            newContext.setChatId(chatId);
            newContext.setCreationDate(LocalDateTime.now());
            save(newContext);
            return newContext;
        }

        return optionalChatContext.get();
    }

    public ReplyState replyState(long chatId) {
        return get(chatId).getReplyState();
    }

    public void replyState(long chatId, ReplyState state) {
        ChatContext context = get(chatId);
        context.setReplyState(state);
        save(context);
    }

    public int lastReplyPage(long chatId) {
        return get(chatId).getLastReplyPage();
    }

    public void lastReplyPage(long chatId, int lastReplyPage) {
        ChatContext context = get(chatId);
        context.setLastReplyPage(lastReplyPage);
        save(context);
    }

    public Branch branch(long chatId) {
        ChatContext context = get(chatId);
        return new Branch(context.getBranchTitle(), context.getBranchValue());
    }

    public void branch(long chatId, Branch branch) {
        ChatContext context = get(chatId);
        context.setBranchTitle(branch.title());
        context.setBranchValue(branch.value());
        save(context);
    }

    public Kit kit(long chatId) {
        ChatContext context = get(chatId);
        return new Kit(context.getKitTitle(), context.getKitValue());
    }

    public void kit(long chatId, Kit kit) {
        ChatContext context = get(chatId);
        context.setKitTitle(kit.title());
        context.setKitValue(kit.value());
        save(context);
    }

    public Group group(long chatId) {
        ChatContext context = get(chatId);
        return new Group(context.getGroupTitle(), context.getGroupValue());
    }

    public void group(long chatId, Group group) {
        ChatContext context = get(chatId);
        context.setGroupTitle(group.title());
        context.setGroupValue(group.value());
        save(context);
    }

    public Employee employee(long chatId) {
        ChatContext context = get(chatId);
        return new Employee(context.getEmployeeTitle(), context.getEmployeeValue());
    }

    public void employee(long chatId, Employee employee) {
        ChatContext context = get(chatId);
        context.setEmployeeTitle(employee.title());
        context.setEmployeeValue(employee.value());
        save(context);
    }

    public boolean isEmployee(long chatId) {
        ChatContext context = get(chatId);
        return context.isEmployee();
    }

    public void isEmployee(long chatId, boolean isEmployee) {
        ChatContext context = get(chatId);
        context.setEmployee(isEmployee);
        save(context);
    }

    public boolean isSetup(long chatId) {
        if (isEmployee(chatId))
            return isEmployeeSetup(chatId);
        else
            return isStudentSetup(chatId);
    }

    public boolean isStudentSetup(long chatId) {
        ChatContext context = get(chatId);
        if (context.getBranchValue() == null) return false;
        if (context.getKitValue() == null) return false;
        return context.getGroupValue() != null;
    }

    public boolean isEmployeeSetup(long chatId) {
        ChatContext context = get(chatId);
        if (context.getBranchValue() == null) return false;
        return context.getEmployeeValue() != null;
    }
}
