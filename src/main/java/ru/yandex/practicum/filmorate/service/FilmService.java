package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validate(film);
        log.info("Создаем новый фильм {}", film);
        return filmStorage.create(film);
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

    public Film likeTheFilm(Long id, Long userId) {
        Film film = filmStorage.findFilmById(id);

        userStorage.findUserById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);

        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        Film film = filmStorage.findFilmById(id);

        userStorage.findUserById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, id);

        return film;
    }

    public Collection<Film> getPopularFilms(int count) {
        List<Film> films  = new ArrayList<>(getAllFilms());
        films.sort((f1, f2) -> f2.getLikes().size() - f1.getLikes().size());
        if (count < films.size()) {
            films = films.subList(0, count);
        }

        log.info("Получен список из {} самых популярных фильмов", count);

        return films;
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
