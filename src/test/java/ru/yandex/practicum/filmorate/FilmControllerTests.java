package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTests {

    FilmController filmController;
    Film film;

    @BeforeEach
    public void beforeEach() {
        UserStorage userStorage = new InMemoryUserStorage();
        FilmStorage filmStorage = new InMemoryFilmStorage((InMemoryUserStorage) userStorage);
        FilmService filmService = new FilmService(filmStorage);
        filmController = new FilmController(filmService);
        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        User user = new User();
        user.setLogin("lodin");
        user.setName("name");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1999, 9, 15));
        userStorage.create(user);
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

    @Test
    public void shouldNotCreateFilmWithNullName() {
        Film newFilm = new Film();
        newFilm.setDescription("Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldNotCreateFilmWithNullReleaseDate() {
        Film newFilm = new Film();
        newFilm.setName("Film");
        newFilm.setDescription("Description");
        newFilm.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(newFilm));
    }

    @Test
    public void shouldLikeTheFilm() {
        filmController.create(film);
        filmController.likeTheFilm(1L, 1L);

        assertTrue(film.getLikes().contains(1L));
        assertTrue(film.getLikes().size() == 1);
    }

    @Test
    public void shouldDeleteLike() {
        filmController.create(film);
        filmController.likeTheFilm(1L, 1L);
        filmController.deleteLike(1L, 1L);

        assertTrue(film.getLikes().isEmpty());
        assertTrue(film.getLikes().size() == 0);
    }

    @Test
    public void shouldGetPopularFilms() {
        filmController.create(film);
        Collection<Film> bestFilms = filmController.getPopularFilms(1);

        assertFalse(bestFilms.isEmpty());
        assertTrue(bestFilms.contains(film));
    }

    @Test
    public void shouldNotGetFilmThatNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmController.likeTheFilm(1L, 1L));

        assertEquals(exception.getMessage(), "Фильм с id = 1 не найден");
    }

    @Test
    public void shouldNotGetPopularFilmBecauseCountIsIncorrect() {
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.getPopularFilms(-1));
        assertEquals("Значение поля count не должно равняться 0 или быть отрицательным", exception.getMessage());

        ValidationException exceptionZero = assertThrows(ValidationException.class, () -> filmController.getPopularFilms(0));
        assertEquals(exceptionZero.getMessage(), "Значение поля count не должно равняться 0 или быть отрицательным");
    }
}
