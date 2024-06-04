package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Set;

public interface LikeStorage {

    public Set<Long> getFilmsLikes(Long id);

    public Film deleteLike(Film film, Long userId);

    public Film likeTheFilm(Film film, Long userId);
}