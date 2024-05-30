package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film create(Film film);
    Collection<Film> getAllFilms();
    Film updateFilm(Film newFilm);
    Film findFilmById(Long id);
    Film likeTheFilm(Long id, Long userId);
    Film deleteLike(Long id, Long userId);
    Collection<Film> getPopularFilms(int count);
}
