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

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }

    private static final String CREATE_FILM_QUERY = "INSERT INTO films(name, description, releaseDate, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_ALL_QUERY = "SELECT f.*, g.genre_id, g.name AS genre_name, m.name as mpa_name " +
            "FROM films f " +
            "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "ORDER BY f.film_id ";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ?, likes_count = ? " +
            "WHERE film_id = ?";

    private static final String FIND_BY_ID_QUERY = "SELECT f.*, m.name as mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "WHERE f.film_id = ?";
    private static final String ADD_GENRES_QUERY = "INSERT INTO film_genre(film_id, genre_id) " +
            "VALUES (?, ?)";
    private static final String GET_POPULAR_QUERY = "SELECT f.*, g.genre_id, g.name AS genre_name, m.name as mpa_name " +
            "FROM films f " +
            "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
            "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
            "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
            "ORDER BY likes_count DESC, f.film_id ASC " +
            "LIMIT ?";

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
        List<Long> filmsId = new ArrayList<>();
        List<Film> films = new ArrayList<>();
        Map<Long, Set<Genre>> genresWithFilms = new HashMap<>();

        jdbcTemplate.query(GET_ALL_QUERY, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            if (!filmsId.contains(filmId)) {
                Film film = new Film();
                film.setId(filmId);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                film.setDuration(rs.getInt("duration"));
                Mpa mpa = new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));
                film.setMpa(mpa);
                film.setLikesCount(rs.getInt("likes_count"));
                films.add(film);
                filmsId.add(filmId);
            } if (rs.getString("genre_name") != null) {
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
                Set<Genre> filmGenres = genresWithFilms.get(filmId);
                if (filmGenres == null) {
                    filmGenres = new HashSet<>();
                    genresWithFilms.put(filmId, filmGenres);
                }
                filmGenres.add(genre);
            }
            return null;
        });
        for (Film film : films) {
            if (genresWithFilms.containsKey(film.getId())) {
                film.setGenres(genresWithFilms.get(film.getId()));
            }
        }
        return films;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        Film oldFilm = findFilmById(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с id = {} не найден" + newFilm.getId());
        }
        int likesCount = oldFilm.getLikesCount();
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
                likesCount,
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
        List<Long> filmsId = new ArrayList<>();
        List<Film> films = new ArrayList<>();
        Map<Long, Set<Genre>> genresWithFilms = new HashMap<>();

        jdbcTemplate.query(GET_POPULAR_QUERY, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            if (!filmsId.contains(filmId)) {
                Film film = new Film();
                film.setId(filmId);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                film.setDuration(rs.getInt("duration"));
                Mpa mpa = new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name"));
                film.setMpa(mpa);
                film.setLikesCount(rs.getInt("likes_count"));
                films.add(film);
                filmsId.add(filmId);
            }
            if (rs.getString("genre_name") != null) {
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("genre_name"));
                Set<Genre> filmGenres = genresWithFilms.get(filmId);
                if (filmGenres == null) {
                    filmGenres = new HashSet<>();
                    genresWithFilms.put(filmId, filmGenres);
                }
                filmGenres.add(genre);
            }
            return null;
        }, count);

        for (Film film : films) {
            if (genresWithFilms.containsKey(film.getId())) {
                film.setGenres(genresWithFilms.get(film.getId()));
            }
        }
        return films;
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        Set<Genre> genres = genreStorage.getFilmGenres(rs.getLong("film_id"));
        if (genres.size() != 0) {
            film.setGenres(genres);
        }
        film.setLikesCount(rs.getInt("likes_count"));
        log.info("Текущее количество лайков в результате запроса: {}", rs.getInt("likes_count"));
        return film;
    }

    private void addGenres(Film film) {
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbcTemplate.batchUpdate(ADD_GENRES_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId().intValue());
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