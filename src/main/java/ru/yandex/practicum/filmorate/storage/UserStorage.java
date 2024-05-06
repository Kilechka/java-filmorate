package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    public User create(User user);

    public Collection<User> getAllUsers();

    public User updateUser(User newUser);

    public User addFriend(Long id, Long userId);

    public User deleteFriend(Long id, Long userId);

    public Collection<User> getAllFriends(Long id);

    public Collection<User> getCommonFriends(Long id, Long otherId);
}
