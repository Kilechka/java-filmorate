package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmDbStorage(@Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate, MpaService mpaService, GenreService genreService, GenreStorage genreStorage) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreStorage = genreStorage;
    }
    private static final String CREATE_FILM_QUERY = "INSERT INTO films(name, description, releaseDate, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_ALL_FILMS_QUERY = "SELECT * FROM films";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String LIKE_QUERY = "INSERT INTO film_likes(user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";
    private static final String ADD_GENRES_QUERY = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "SELECT f.*, COUNT (DISTINCT fl.user_id) as likes_count " +
            "FROM films f " +
            "JOIN film_likes fl ON f.film_id = fl.film_id " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC " +
            "LIMIT ?";
    private static final String GET_LIKES_QUERY = "SELECT * FROM film_likes";

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Mpa mpa = film.getMpa();
        Set<Genre> genres = null;
        if (!film.getGenres().isEmpty()) {
            log.info("добавляем имена жанрам");
            genres = addNameToGenre(film.getGenres());
            film.setGenres(genres);
        }
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM_QUERY, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, mpa.getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        if (!film.getGenres().isEmpty()) {
            log.info("добавляем жанры {}", genres);
            addGenres(film);
            log.info("добавляем жанры {}", genres);
        }
        log.info("Создан фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return jdbcTemplate.query(GET_ALL_FILMS_QUERY, this::makeFilm);
    }

    @Override
    public Film updateFilm(Film newFilm) {
        Film oldFilm = findFilmById(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id = {} не найден" + newFilm.getId());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Mpa mpa = newFilm.getMpa();
        Set<Genre> genres = null;
        if (!newFilm.getGenres().isEmpty()) {
            log.info("добавляем имена жанрам");
            genres = addNameToGenre(newFilm.getGenres());
            newFilm.setGenres(genres);
        }

        jdbcTemplate.update(UPDATE_FILM_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                toSqlDate(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId());
        if (newFilm.getGenres() != null || !newFilm.getGenres().isEmpty()) {
            log.info("добавляем жанры {}", newFilm.getGenres());
            jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", newFilm.getId());
            addGenres(newFilm);
        }
        log.info("Обновлен фильм с id = {}", newFilm.getId());
        return newFilm;
    }

    @Override
    public Film findFilmById(Long id) {
        final List<Film> films = jdbcTemplate.query(FIND_BY_ID_QUERY, this::makeFilm, id);
        if (films.size() != 1) {
            throw new NotFoundException("Не найден фильм с id = " + id);
        }
        return films.get(0);
    }

    @Override
    public Film likeTheFilm(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userStorage.findUserById(userId);

        jdbcTemplate.update(LIKE_QUERY, userId, id);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);

        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        Film film = findFilmById(id);
        User user = userStorage.findUserById(userId);

        jdbcTemplate.update(DELETE_LIKE_QUERY, userId, id);
        log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, id);
        return film;
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("Получен список из {} самых популярных фильмов", count);
        return jdbcTemplate.query(GET_POPULAR_QUERY, this::makeFilm, count);
    }

    public Collection<Integer> getFilmsLikes(Long id) {
        List<Integer> likes = new ArrayList<>();

        SqlRowSet rows = jdbcTemplate.queryForRowSet(GET_LIKES_QUERY);
        while (rows.next()) {
            if (rows.getInt("film_id") == id) {
                likes.add(rows.getInt("user_id"));
            }
        }
        return likes;
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        Mpa mpa = mpaService.getMpaById(rs.getInt("mpa_id"));
        film.setMpa(mpa);
        Set<Genre> genres = genreStorage.getFilmGenres(rs.getLong("film_id"));
        if (genres.size() != 0) {
            film.setGenres(genreStorage.getFilmGenres(rs.getLong("film_id")));
        }
        return film;
    }

    private void addGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(ADD_GENRES_QUERY, film.getId(), genre.getId());
        }
    }

    private Set<Genre> addNameToGenre(Set<Genre> genres) {
        for (Genre genre : genres) {
            String genreName = genreStorage.getGenreByName(genre.getId());
            genre.setName(genreName);
        }
        return genres;
    }

    private Date toSqlDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }
}
