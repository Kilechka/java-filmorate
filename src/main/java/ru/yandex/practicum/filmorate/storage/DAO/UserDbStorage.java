package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String CREATE_USER_QUERY = "INSERT INTO users(login, email, birthday, name)" +
            "VALUES (?, ?, ?, ?)";
    private static final String GET_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET login = ?, email = ?, birthday = ?, name = ? WHERE user_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String GET_ALL_FRIENDS_QUERY = "SELECT u.* " +
            "FROM users AS u " +
            "JOIN friendship AS f ON u.user_id = f.friend_id " +
            "WHERE f.user_id = ?";
    private static final String DELETE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_USER_QUERY, new String[]{"user_id"});
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getEmail());
            ps.setDate(3, java.sql.Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getName());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        log.info("Создан пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(GET_ALL_USERS_QUERY, this::makeUser);
    }

    @Override
    public User updateUser(User newUser) {
        User oldUser = findUserById(newUser.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id = " + newUser.getId() + " не найден");
            throw new NotFoundException("Не найден пользователь с id = " + newUser.getId());
        }
        jdbcTemplate.update(UPDATE_USER_QUERY,
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getBirthday(),
                newUser.getName(),
                newUser.getId());
        log.info("Обновлен пользователь с id = {}", newUser.getId());
        return newUser;
    }

    @Override
    public User findUserById(Long id) {
        final List<User> users = jdbcTemplate.query(FIND_BY_ID_QUERY, this::makeUser, id);
        if (users.size() != 1) {
            log.warn("Пользователь с id = " + id + " не найден");
            throw new NotFoundException("Не найден пользователь с id = " + id);
        }
        log.info("Найден пользователь с id = {}", id);
        return users.get(0);
    }

    @Override
    public User addFriend(Long id, Long otherId) {
        User user = findUserById(id);
        User otherUser = findUserById(otherId);

        jdbcTemplate.update(ADD_FRIEND_QUERY, id, otherId);
        log.info("Теперь мы есть в списке друзей у пользователя = " + otherId);

        return otherUser;
    }

    @Override
    public User deleteFriend(Long id, Long userId) {
        User user = findUserById(id);
        User otherUser = findUserById(userId);
        jdbcTemplate.update(DELETE_FRIEND, id, userId);
        log.info("У пользователя с id {} больше нет в друзьях пользователя с id {} ", userId, id);
        return otherUser;
    }

    @Override
    public Collection<User> getAllFriends(Long id) {
        User user = findUserById(id);
        log.info("Получен список друзей пользователя с id " + id);
        return jdbcTemplate.query(GET_ALL_FRIENDS_QUERY, this::makeUser, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = findUserById(id);
        User otherUser = findUserById(otherId);
        List<User> friends = jdbcTemplate.query(GET_ALL_FRIENDS_QUERY, this::makeUser, id);
        List<User> otherFriends = jdbcTemplate.query(GET_ALL_FRIENDS_QUERY, this::makeUser, otherId);
        friends.retainAll(otherFriends);
        log.info("Получен список общих друзей пользователей с id {} и {} ", id, otherId);
        return friends;
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setName(rs.getString("name"));
        return user;
    }
}
