package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User create(User user);

    Collection<User> getAllUsers();

    User updateUser(User newUser);

    User findUserById(Long id);

    User addFriend(Long id, Long userId);

    User deleteFriend(Long id, Long userId);

    Collection<User> getAllFriends(Long id);
    
    Collection<User> getCommonFriends(Long id, Long otherId);
}