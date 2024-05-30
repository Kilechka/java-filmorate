package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, LikeStorage likeStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;
    }

    private static final String CREATE_FILM_QUERY = "INSERT INTO films(name, description, releaseDate, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_ALL_FILMS_QUERY = "SELECT f.*, m.name as mpa_name " +
            "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ? " +
            "WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT f.*, m.name as mpa_name " +
            "FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
    private static final String ADD_GENRES_QUERY = "INSERT INTO film_genre(film_id, genre_id) " +
            "VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "SELECT f.*, COUNT (DISTINCT fl.user_id) as likes_count, mpa.name as mpa_name " +
            "FROM films f JOIN film_likes fl ON f.film_id = fl.film_id " +
            "LEFT JOIN mpa ON f.mpa_id = mpa.mpa_id " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC LIMIT ?";

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
        Set<Genre> genres;
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
    public Collection<Film> getPopularFilms(int count) {
        log.info("Получен список из {} самых популярных фильмов", count);
        return jdbcTemplate.query(GET_POPULAR_QUERY, this::makeFilm, count);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        film.setLikes(likeStorage.getFilmsLikes(rs.getLong("film_id")));
        Set<Genre> genres = genreStorage.getFilmGenres(rs.getLong("film_id"));
        if (genres.size() != 0) {
            film.setGenres(genreStorage.getFilmGenres(rs.getLong("film_id")));
        }
        return film;
    }

    private void addGenres(Film film) {
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate(ADD_GENRES_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, Math.toIntExact(film.getId()));
                ps.setInt(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
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