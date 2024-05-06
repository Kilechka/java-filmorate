package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Выполняем Post-запрос");
        return filmService.create(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Выполняем Get-запрос");
        return filmService.getAllFilms();
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Выполняем Put-запрос");
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film likeTheFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Выполняем Put-запрос");
        return filmService.likeTheFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Выполняем Delete-запрос");
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") int count) {
        if (count < 1) {
            throw new ValidationException("Значение поля count не должно равняться 0 или быть отрицательным");
        }
        log.info("Выполняем Get-запрос");
        return filmService.getPopularFilms(count);
    }
}
