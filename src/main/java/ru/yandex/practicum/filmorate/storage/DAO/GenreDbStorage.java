package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String GET_GENRES_QUERY = "SELECT * FROM genre";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String  GET_GENRES_NAME_QUERY = "SELECT name FROM genre WHERE genre_id = ?";

    @Override
    public Collection<Genre> getAllGenres() {
        return jdbcTemplate.query(GET_GENRES_QUERY, this::makeGenre);
    }

    @Override
    public Genre getGenreById(int id) {
        List<Genre> genres = jdbcTemplate.query(GET_BY_ID_QUERY, this::makeGenre, id);
        if (genres.size() != 1) {
            log.warn("Не найден жанр с id = " + id);
            throw new NotFoundException("Не найден жанр с id = " + id);
        }
        log.info("Получен жанр с id = " + id);
        return genres.get(0);
    }

    @Override
    public Set<Genre> getFilmGenres(Long id) {
        String sql = "SELECT * FROM film_genre JOIN genre ON film_genre.genre_id = genre.genre_id WHERE film_id = ?";
        return new HashSet<>(
                jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(
                                rs.getInt("genre_id"),
                                rs.getString("name")),
                        id
                ));
    }

    public String getGenreByName(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_GENRES_NAME_QUERY, String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        String name = rs.getString("name");
        int id = rs.getInt("genre_id");
        Genre genre = new Genre(id, name);
        return genre;
    }
}
