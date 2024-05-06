package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

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
    public User addFriend(Long id, Long userId) {
        User user = findUserById(id);
        User friend = findUserById(userId);

        user.getFriends().add(userId);
        friend.getFriends().add(id);
        log.info("Пользователь с id {} добавил в друзья пользователя с id {} ", id, userId);

        return friend;
    }

    @Override
    public User deleteFriend(Long id, Long userId) {
        User user = findUserById(id);
        User friend = findUserById(userId);

        if (user.getFriends().contains(userId)) {
            user.getFriends().remove(userId);
            friend.getFriends().remove(id);
            log.info("Пользователь с id {} удалил из друзей пользователя с id {} ", id, userId);
        }

        return friend;
    }

    public Collection<User> getAllFriends(Long id) {
        User user = findUserById(id);
        Set<Long> friendsId = user.getFriends();
        log.info("Получен список друзей пользователя с id " + id);

        return friendsId.stream().map(this::findUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = findUserById(id);
        User otherUser = findUserById(otherId);
        Set<Long> userFriendsId = user.getFriends();
        Set<Long> otherFriendsId = otherUser.getFriends();

        Set<Long> commonFriendsId = new HashSet<>(userFriendsId);
        commonFriendsId.retainAll(otherFriendsId);
        log.info("Получен список общих друзей пользователей с id {} и {} ", id, otherId);

        return commonFriendsId.stream()
                .map(this::findUserById)
                .collect(Collectors.toSet());
    }

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
