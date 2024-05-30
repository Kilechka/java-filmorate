package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос на создание нового пользователя {}", user);
        return userService.create(user);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя {}", newUser);
        return userService.updateUser(newUser);
    }

    @GetMapping("/{id}")
    public User findUserById(Long id) {
        log.info("Получен запрос на получение пользователя с id = {}", id);
        return userService.findUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос от пользователя {} на добавление пользователя {} в друзья", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос от пользователя {} на удаление пользователя {} из друзей", id, friendId);
        return userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение всех друзей пользователя {}", id);
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос от пользователя {} на получение всех общих друзей с пользователем {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
