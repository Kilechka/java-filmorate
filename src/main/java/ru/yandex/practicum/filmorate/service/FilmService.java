package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, @Qualifier("UserDbStorage") UserStorage userStorage, GenreService genreService, MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validate(film);
        log.info("Создаем новый фильм {}", film);
        filmStorage.create(film);
        return film;
    }

    public Collection<Film> getAllFilms() {
        log.info("Получаем список всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film updateFilm(Film newFilm) {
        validate(newFilm);
        if (newFilm.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Обновляем фильм {}", newFilm);
        return filmStorage.updateFilm(newFilm);
    }

    public Film findFilmById(Long id) {
        return filmStorage.findFilmById(id);
    }

    public Film likeTheFilm(Long id, Long userId) {
        log.info("Пользователь с id {} ставит лайк фильму с id {}", userId, id);
        return filmStorage.likeTheFilm(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        Film film = filmStorage.findFilmById(id);
        userStorage.findUserById(userId);

        filmStorage.deleteLike(id, userId);
        log.info("Пользователь с id {} удаляет лайк у фильма с id {}", userId, id);

        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Получаем список из {} самых популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }

    private void validate(Film film) {
        Optional<Genre> wrongGenres= film.getGenres().stream()
                .filter(genre -> genre.getId() > 6 || genre.getId() < 1)
                .findFirst();
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.warn("Описание не может быть пустым");
            throw new ValidationException("Описание не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Максимальная длина описания — 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза не может быть пустой");
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Дата релиза должна быть не раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("Продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getMpa().getId() > 5 || film.getMpa().getId() < 1) {
            throw new ValidationException("Указан некорректный рейтинг");
        }
        if (wrongGenres.isPresent()) {
            throw new ValidationException("Указан некорректный жанр");
        }
        log.info("Валидация пройдена");
    }
}
