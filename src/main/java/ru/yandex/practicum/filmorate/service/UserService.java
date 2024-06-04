package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        validate(user);
        log.info("Создаем нового пользователя {}", user);
        return userStorage.create(user);
    }

    public Collection<User> getAllUsers() {
        log.info("Получаем список всех пользователей");
        return userStorage.getAllUsers();
    }

    public User updateUser(User newUser) {
        validate(newUser);
        if (newUser.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Обновляем пользователя {}", newUser);
        return userStorage.updateUser(newUser);
    }

    public User findUserById(Long id) {
        log.info("Получаем пользователя с id = {}" + id);
        return  userStorage.findUserById(id);
    }

    public User addFriend(Long id, Long userId) {
        return userStorage.addFriend(id, userId);
    }

    public User deleteFriend(Long id, Long userId) {
        return userStorage.deleteFriend(id, userId);
    }

    public Collection<User> getAllFriends(Long id) {
        return userStorage.getAllFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Валидация пройдена");
    }
}
