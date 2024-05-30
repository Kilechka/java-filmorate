package ru.yandex.practicum.filmorate.daoTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.DAO.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:schemaTest.sql")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTests {

    private final UserDbStorage userDbStorage;
    private final UserService userService;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setLogin("login");
        user1.setName("name");
        user1.setEmail("email@example.com");
        user1.setBirthday(LocalDate.of(1999, 9, 15));
        user2 = new User();
        user2.setLogin("Login2");
        user2.setName("Name2");
        user2.setEmail("email2@example.com");
        user2.setBirthday(LocalDate.of(1999, 9, 15));
        user3 = new User();
        user3.setLogin("login3");
        user3.setName("name3");
        user3.setEmail("email3@example.com");
        user3.setBirthday(LocalDate.of(1999, 9, 15));

        userService.create(user1);
        userService.create(user2);
        userService.create(user3);
    }

    @Test
    public void shouldFindById() {
        User user = userDbStorage.findUserById(1L);

        assertTrue(user != null);
        assertTrue(user.getLogin().equals("login"));
    }

    @Test
    public void shouldGetAllUsers() {
        Collection<User> users = userService.getAllUsers();

        assertTrue(users != null);
        assertTrue(users.size() == 3);
    }

    @Test
    public void shouldUpdateUser() {
        User newUser = new User();
        newUser.setId(1L);
        newUser.setBirthday(LocalDate.of(1000, 9, 15));
        newUser.setEmail("newEmail@yandex.ru");
        newUser.setLogin("NewLogin");

        userService.updateUser(newUser);

        assertTrue(userDbStorage.findUserById(1L).getEmail().equals(newUser.getEmail()));
        assertTrue(userDbStorage.findUserById(1L).getName().equals("NewLogin"));
        assertTrue(userDbStorage.findUserById(1L).getLogin().equals("NewLogin"));
    }

    @Test
    public void shouldAddFriend() {
        userService.addFriend(1L, 2L);

        assertEquals(1, userService.getAllFriends(1L).size());
        assertEquals(0, userService.getAllFriends(2L).size());

        userService.addFriend(2L, 1L);

        assertEquals(1, userService.getAllFriends(1L).size());
        assertEquals(1, userService.getAllFriends(2L).size());
        assertTrue(userService.getAllFriends(1L).contains(user2));
        assertTrue(userService.getAllFriends(2L).contains(user1));
    }

    @Test
    public void shouldDeleteFriend() {
        userService.addFriend(1L, 2L);
        userService.addFriend(2L, 1L);

        assertEquals(1, userService.getAllFriends(1L).size());
        assertEquals(1, userService.getAllFriends(2L).size());

        userService.deleteFriend(1L, 2L);

        assertEquals(0, userService.getAllFriends(1L).size());
        assertEquals(1, userService.getAllFriends(2L).size());
    }

    @Test
    public void shouldGetAllFriends() {
        userService.addFriend(1L, 2L);
        userService.addFriend(2L, 1L);

        Collection<User> friends = userService.getAllFriends(1L);

        assertTrue(friends != null);
        assertTrue(friends.size() == 1);
        assertTrue(friends.contains(user2));
    }

    @Test
    public void shouldGetCommonFriends() {
        userService.addFriend(1L, 2L);
        userService.addFriend(2L, 1L);
        userService.addFriend(1L, 3L);
        userService.addFriend(3L, 1L);
        userService.addFriend(2L, 3L);
        userService.addFriend(3L, 2L);

        Collection<User> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends != null);
        assertTrue(commonFriends.size() == 1);
        assertTrue(commonFriends.contains(user3));
    }
}
