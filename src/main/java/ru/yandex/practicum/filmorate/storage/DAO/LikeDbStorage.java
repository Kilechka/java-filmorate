package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
@Slf4j
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String LIKE_QUERY = "INSERT INTO film_likes(user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";
    private static final String GET_LIKES_QUERY = "SELECT * FROM film_likes";

    @Override
    public Film likeTheFilm(Film film, Long userId) {
        jdbcTemplate.update(LIKE_QUERY, userId, film.getId());
        jdbcTemplate.update("UPDATE films SET likes_count = likes_count + 1 WHERE film_id = ?", film.getId());
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, film.getId());

        return film;
    }

    @Override
    public Film deleteLike(Film film, Long userId) {
        jdbcTemplate.update(DELETE_LIKE_QUERY, userId, film.getId());
        jdbcTemplate.update("UPDATE films SET likes_count = likes_count - 1 WHERE film_id = ?", film.getId());
        log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, film.getId());

        return film;
    }

    @Override
    public Set<Long> getFilmsLikes(Long id) {
        Set<Long> likes = new HashSet<>();

        SqlRowSet rows = jdbcTemplate.queryForRowSet(GET_LIKES_QUERY);
        while (rows.next()) {
            if (rows.getInt("film_id") == id) {
                likes.add(rows.getLong("user_id"));
            }
        }

        return likes;
    }
}