package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {

    UserController userController;
    User user;
    User otherUser;

    @BeforeEach
    public void beforeEach() {
        InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();
        UserService userService = new UserService(inMemoryUserStorage);
        userController = new UserController(inMemoryUserStorage, userService);
        user = new User();
        user.setLogin("lodin");
        user.setName("name");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1999, 9, 15));
        otherUser = new User();
        otherUser.setLogin("otherLogin");
        otherUser.setName("otherName");
        otherUser.setEmail("otherEmail@example.com");
        otherUser.setBirthday(LocalDate.of(1999, 9, 15));
        userController.create(otherUser);
    }

    @Test
    public void shouldPostAndGetUser() {
        userController.create(user);

        assertTrue(!userController.getAllUsers().isEmpty());
        assertTrue(userController.getAllUsers().size() == 2);
    }

    @Test
    public void shouldUpdateUser() {
        userController.create(user);
        User newUser = new User();
        newUser.setId(1L);
        newUser.setBirthday(LocalDate.of(1999, 9, 15));
        newUser.setEmail("kilechka@yandex.ru");
        newUser.setLogin("Lilichka");

        userController.updateUser(newUser);

        Optional<User> optionalUser =  userController.getAllUsers().stream().filter(u -> u.getId() == newUser.getId()).findFirst();
        User updatedUser = optionalUser.orElseThrow(() -> new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден"));

        assertTrue(updatedUser.getLogin().equals("Lilichka"));
        assertTrue(updatedUser.getName().equals("Lilichka"));
    }

    @Test
    public void shouldNotCreateUserWithEmptyEmail() {
        User newUser = new User();
        newUser.setEmail("");
        newUser.setLogin("login");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(newUser));
    }

    @Test
    public void shouldNotCreateUserWithEmptyLogin() {
        User newUser = new User();
        newUser.setEmail("email@example.com");
        newUser.setLogin("");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(newUser));
    }

    @Test
    public void shouldNotCreateUserWithLoginContainingSpaces() {
        User newUser = new User();
        newUser.setEmail("email@example.com");
        newUser.setLogin("login with spaces");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(newUser));
    }

    @Test
    public void shouldNotCreateUserWithFutureBirthday() {
        User newUser = new User();
        newUser.setEmail("email@example.com");
        newUser.setLogin("login");
        newUser.setName("name");
        newUser.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(newUser));
    }

    @Test
    public void shouldNotUpdateUserWithoutId() {
        User newUser = new User();
        newUser.setId(null);
        newUser.setEmail("newEmail@example.com");
        newUser.setLogin("newLogin");
        newUser.setName("newName");
        newUser.setBirthday(LocalDate.of(1999, 12, 31));

        assertThrows(ValidationException.class, () -> userController.updateUser(newUser));
    }

    @Test
    public void shouldNotUpdateUserWhenUserNotFound() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("newEmail@example.com");
        newUser.setLogin("newLogin");
        newUser.setName("newName");
        newUser.setBirthday(LocalDate.of(1999, 12, 31));

        assertThrows(NotFoundException.class, () -> userController.updateUser(newUser));
    }

    @Test
    public void shouldAddFriend() {
        userController.create(user);

        userController.addFriend(1L, 2L);

        assertTrue(user.getFriends().contains(otherUser.getId()));
        assertTrue(otherUser.getFriends().contains(user.getId()));
    }

    @Test
    public void shouldDeleteFriend() {
        userController.create(user);
        userController.addFriend(1L, 2L);
        userController.deleteFriend(1L, 2L);

        Collection<User> friends = userController.getAllFriends(1L);
        assertTrue(friends.isEmpty());
    }

    @Test
    public void shouldGetAllFriends() {
        userController.create(user);
        userController.addFriend(1L, 2L);

        Collection<User> friends = userController.getAllFriends(1L);

        assertTrue(friends.contains(user));
        assertFalse(friends.isEmpty());
        assertTrue(friends.size() == 1);
    }

    @Test
    public void shouldGetCommonFriends() {
        userController.create(user);
        User commonUser = new User();
        commonUser.setLogin("lodin");
        commonUser.setName("name");
        commonUser.setEmail("email@example.com");
        commonUser.setBirthday(LocalDate.of(1999, 9, 15));
        userController.create(commonUser);
        userController.addFriend(1L, 3L);
        userController.addFriend(2L, 3L);

        Collection<User> commonFriends = userController.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.contains(commonUser));
        assertTrue(commonFriends.size() == 1);
    }

    @Test
    public void shouldNotGetUserThatNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.addFriend(1L, 5L));

        assertEquals(exception.getMessage(), "Пользователь с id = 5 не найден");
    }
}
