package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        validate(film);
        log.info("Выполняем метод create в сервисе");
        return filmStorage.create(film);
    }

    public Collection<Film> getAllFilms() {
        log.info("Выполняем метод getAllFilms в сервисе");
        return filmStorage.getAllFilms();
    }

    public Film updateFilm(Film newFilm) {
        validate(newFilm);
        if (newFilm.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Выполняем метод updateFilm в сервисе");
        return filmStorage.updateFilm(newFilm);
    }

    public Film likeTheFilm(Long id, Long userId) {
        log.info("Выполняем метод likeTheFilm в сервисе");
        return filmStorage.likeTheFilm(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Выполняем метод deleteLike в сервисе");
        return filmStorage.deleteLike(id,userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Выполняем метод getPopularFilms в сервисе");
        return filmStorage.getPopularFilms(count);
    }

    private void validate(Film film) {
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
        log.info("Валидация пройдена");
    }
}
