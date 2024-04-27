package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTests {

    FilmController filmController;
    Film film;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
    }

    @Test
    public void shouldPostAndGetFilm() {
        filmController.create(film);

        Film sameFilm = new Film();
        sameFilm.setId(1L);
        sameFilm.setName("Film");
        sameFilm.setDescription("Description");
        sameFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        sameFilm.setDuration(120);

        assertTrue(!filmController.getAllFilms().isEmpty());
        assertTrue(filmController.getAllFilms().size() == 1);
        assertTrue(filmController.getAllFilms().contains(sameFilm));
    }

    @Test
    public void shouldUpdateFilm() {
        filmController.create(film);
        Film newFilm = new Film();
        newFilm.setId(1L);
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        filmController.updateFilm(newFilm);

        Optional<Film> optionalFilm = filmController.getAllFilms().stream().filter(f -> f.getId() == newFilm.getId()).findFirst();
        Film updatedFilm = optionalFilm.orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден"));

        assertTrue(updatedFilm.getName().equals("New Film"));
        assertTrue(updatedFilm.getDescription().equals("New Description"));
    }

    @Test
    public void shouldNotCreateFilmWithEmptyName() {
        Film newFilm = new Film();
        newFilm.setName("");
        newFilm.setDescription("Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldNotCreateFilmWithLongDescription() {
        Film newFilm = new Film();
        newFilm.setName("Film");
        newFilm.setDescription("Очень-очень большое описание к фильму. Очень-очень большое описание к фильму. Очень-очень большое описание к фильму." +
                "Очень-очень большое описание к фильму. Очень-очень большое описание к фильму. Очень-очень большое описание к фильму." +
                "Очень-очень большое описание к фильму. Очень-очень большое описание к фильму.");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldNotCreateFilmWithEarlyReleaseDate() {
        Film newFilm = new Film();
        newFilm.setName("Film");
        newFilm.setDescription("Description");
        newFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldNotCreateFilmWithNegativeDuration() {
        Film newFilm = new Film();
        newFilm.setName("Film");
        newFilm.setDescription("Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(-120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldNotUpdateFilmWithoutId() {
        Film newFilm = new Film();
        newFilm.setId(null);
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.updateFilm(newFilm));
    }

    @Test
    public void shouldNotUpdateFilmWhenFilmNotFound() {
        Film newFilm = new Film();
        newFilm.setId(2L);
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        assertThrows(NotFoundException.class, () -> filmController.updateFilm(newFilm));
    }
}
