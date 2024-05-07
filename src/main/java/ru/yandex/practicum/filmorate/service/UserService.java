package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
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

    public User addFriend(Long id, Long userId) {
        User user = userStorage.findUserById(id);
        User friend = userStorage.findUserById(userId);

        user.getFriends().add(userId);
        friend.getFriends().add(id);
        log.info("Пользователь с id {} добавил в друзья пользователя с id {} ", id, userId);

        return friend;
    }

    public User deleteFriend(Long id, Long userId) {
        User user = userStorage.findUserById(id);
        User friend = userStorage.findUserById(userId);

        if (user.getFriends().contains(userId)) {
            user.getFriends().remove(userId);
            friend.getFriends().remove(id);
            log.info("Пользователь с id {} удалил из друзей пользователя с id {} ", id, userId);
        }

        return friend;
    }

    public Collection<User> getAllFriends(Long id) {
        User user = userStorage.findUserById(id);
        Set<Long> friendsId = user.getFriends();
        log.info("Получен список друзей пользователя с id " + id);

        return friendsId.stream().map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = userStorage.findUserById(id);
        User otherUser = userStorage.findUserById(otherId);
        Set<Long> userFriendsId = user.getFriends();
        Set<Long> otherFriendsId = otherUser.getFriends();

        Set<Long> commonFriendsId = new HashSet<>(userFriendsId);
        commonFriendsId.retainAll(otherFriendsId);
        log.info("Получен список общих друзей пользователей с id {} и {} ", id, otherId);

        return commonFriendsId.stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toSet());
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
