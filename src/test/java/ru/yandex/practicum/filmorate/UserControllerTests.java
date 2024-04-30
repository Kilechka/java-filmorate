package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {

    UserController userController;
    User user;

    @BeforeEach
    public void beforeEach() {
        userController = new UserController();
        user = new User();
        user.setLogin("lodin");
        user.setName("name");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1999, 9, 15));
    }

    @Test
    public void shouldPostAndGetUser() {
        userController.create(user);

        User sameUser = new User();
        sameUser.setId(1L);
        sameUser.setEmail("email@example.com");
        sameUser.setName("name");
        sameUser.setLogin("lodin");
        sameUser.setBirthday(LocalDate.of(1999, 9, 15));

        assertTrue(!userController.getAllUsers().isEmpty());
        assertTrue(userController.getAllUsers().size() == 1);
        assertTrue(userController.getAllUsers().contains(sameUser));
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
}
