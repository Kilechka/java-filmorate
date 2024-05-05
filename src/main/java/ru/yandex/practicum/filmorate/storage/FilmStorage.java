package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Film create(Film film);

    public Collection<Film> getAllFilms();

    public Film updateFilm(Film newFilm);
}