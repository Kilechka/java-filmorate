package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film create(Film film);

    Collection<Film> getAllFilms();

    Film updateFilm(Film newFilm);

    Film findFilmById(Long id);

    Collection<Film> getPopularFilms(int count);
}