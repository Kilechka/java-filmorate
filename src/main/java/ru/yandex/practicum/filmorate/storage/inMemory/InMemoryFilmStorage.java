package ru.yandex.practicum.filmorate.storage.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private Long idForFilm = 0L;
    private final UserStorage userStorage;

    @Autowired
    public InMemoryFilmStorage(@Qualifier("InMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film film = films.get(newFilm.getId());
            film.setName(newFilm.getName());
            film.setDescription(newFilm.getDescription());
            film.setReleaseDate(newFilm.getReleaseDate());
            film.setDuration(newFilm.getDuration());
            log.info("Обновлен фильм с id = {}", newFilm.getId());
            return film;
        }
        log.warn("Фильм с id = " + newFilm.getId() + " не найден");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Film likeTheFilm(Long id, Long userId) {
        Film film = findFilmById(id);

        userStorage.findUserById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);

        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        Film film = findFilmById(id);

        userStorage.findUserById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, id);

        return film;
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<Film> films  = new ArrayList<>(getAllFilms());
        films.sort((f1, f2) -> f2.getLikes().size() - f1.getLikes().size());
        if (count < films.size()) {
            films = films.subList(0, count);
        }

        log.info("Получен список из {} самых популярных фильмов", count);

        return films;
    }

    @Override
    public Film findFilmById(Long id) {
        if (!films.containsKey(id)) {
            log.warn("Фильм с id = " + id + " не найден");
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return films.get(id);
    }

    private Long getNextId() {
        return ++idForFilm;
    }
}