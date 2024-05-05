package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserStorage userStorage;

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
}
