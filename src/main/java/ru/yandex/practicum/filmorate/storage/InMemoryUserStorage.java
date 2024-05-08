package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long idForUser = 0L;

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User updateUser(User newUser) {
        if (users.containsKey(newUser.getId())) {
            User user = users.get(newUser.getId());
            user.setName(newUser.getName());
            user.setLogin(newUser.getLogin());
            user.setEmail(newUser.getEmail());
            user.setBirthday(newUser.getBirthday());
            log.info("Обновлен пользователь с id = {}", newUser.getId());
            return user;
        }
        log.warn("Пользователь с id = " + newUser.getId() + " не найден");
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            log.warn("Пользователь с id = " + id + " не найден");
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    private Long getNextId() {
        return ++idForUser;
    }
}
