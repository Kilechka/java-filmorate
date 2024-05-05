package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

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
        if (count < 0 || count == 0) {
            log.warn("Значение поля count не должно равняться 0 или быть отрицательным");
            throw new ValidationException("Значение поля count не должно равняться 0 или быть отрицательным");
        }
        List<Film> films  = new ArrayList<>(filmStorage.getAllFilms());
        films.sort((f1, f2) -> f2.getLikes().size() - f1.getLikes().size());
        if (count < films.size()) {
            films = films.subList(0, count);
        }

        log.info("Получен список из {} самых популярных фильмов", count);

        return films;
    }
}
