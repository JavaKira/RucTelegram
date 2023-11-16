package com.github.javakira.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserContextServiceImpl implements UserContextService {
    private final UserContextRepository repository;

    @Override
    public UserContext userContext(User user) {
        Optional<UserContext> optional = repository.findById(user.getId());
        return optional.orElseGet(() -> newUserContext(user));
    }

    @Override
    public void update(User user) {
        repository.save(updateUserContext(user, userContext(user)));
    }

    private UserContext updateUserContext(User user, UserContext context) {
        return new UserContext(
                user.getId(),
                context.getCreationDate(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                user.getIsPremium() != null
        );
    }

    private UserContext newUserContext(User user) {
        return new UserContext(
                user.getId(),
                LocalDateTime.now(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserName(),
                user.getIsPremium() != null
        );
    }
}
