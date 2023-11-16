package com.github.javakira.context;

import org.telegram.telegrambots.meta.api.objects.User;

public interface UserContextService {
    /**@return UserContext of ...api.objects.User. If it doesn't exist, then it create new UserContext and return it*/
    UserContext userContext(User user);

    /**Patch UserContext of ...api.objects.User. If it doesn't exist, then it create new UserContext and return it*/
    void update(User user);
}
