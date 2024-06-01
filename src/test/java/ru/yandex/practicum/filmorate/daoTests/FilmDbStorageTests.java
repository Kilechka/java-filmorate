package ru.yandex.practicum.filmorate.daoTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.DAO.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.DAO.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:schemaTest.sql")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTests {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final MpaService mpaService;
    private final FilmService filmService;
    private final GenreService genreService;
    private final LikeStorage likeStorage;
    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;
    private User user3;
    Mpa mpa;
    Mpa mpa2;

    @BeforeEach
    public void setUp() {
        mpa = mpaService.getMpaById(1);
        mpa2 = mpaService.getMpaById(2);
        Genre genre = genreService.getGenreById(1);
        Genre genre1 = genreService.getGenreById(2);
        Set<Genre> genres = Set.of(genre, genre1);

        user1 = new User();
        user1.setLogin("login");
        user1.setName("name");
        user1.setEmail("email@example.com");
        user1.setBirthday(LocalDate.of(1999, 9, 15));
        user2 = new User();
        user2.setLogin("Login2");
        user2.setName("Name2");
        user2.setEmail("email2@example.com");
        user2.setBirthday(LocalDate.of(1999, 9, 15));
        user3 = new User();
        user3.setLogin("login3");
        user3.setName("name3");
        user3.setEmail("email3@example.com");
        user3.setBirthday(LocalDate.of(1999, 9, 15));

        userDbStorage.create(user1);
        userDbStorage.create(user2);
        userDbStorage.create(user3);

        film1 = new Film();
        film1.setName("Film1");
        film1.setDescription("Description1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(100);
        film1.setGenres(genres);
        film1.setMpa(mpa);
        filmService.create(film1);

        film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("Description2");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(200);
        film2.setMpa(mpa);
        filmService.create(film2);

        film3 = new Film();
        film3.setName("Film3");
        film3.setDescription("Description3");
        film3.setReleaseDate(LocalDate.of(2020, 1, 1));
        film3.setDuration(300);
        film3.setMpa(mpa);
        filmService.create(film3);

    }

    @Test
    public void shouldFindById() {
        Film film = filmDbStorage.findFilmById(1L);

        assertTrue(film != null);
        assertTrue(film.getName().equals("Film1"));
    }

    @Test
    public void shouldGetAllFilms() {
        Collection<Film> films = filmService.getAllFilms();
        System.out.println(film1.getGenres());

        assertTrue(films != null);
        assertTrue(films.size() == 3);
    }

    @Test
    public void shouldUpdateFilm() {
        Film newFilm = new Film();
        newFilm.setId(1L);
        newFilm.setName("NewFilm");
        newFilm.setDescription("NewDescription");
        newFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        newFilm.setDuration(500);
        newFilm.setMpa(mpa2);

        filmService.updateFilm(newFilm);

        assertTrue(filmDbStorage.findFilmById(1L).getName().equals("NewFilm"));
        assertTrue(filmDbStorage.findFilmById(1L).getDescription().equals("NewDescription"));
        assertTrue(filmDbStorage.findFilmById(1L).getReleaseDate().equals(LocalDate.of(2021, 1, 1)));
        assertTrue(filmDbStorage.findFilmById(1L).getDuration() == 500);
    }

    @Test
    public void shouldLikeTheFilm() {
        filmService.likeTheFilm(1L, 1L);

        assertTrue(likeStorage.getFilmsLikes(1L).size() == 1);
    }

    @Test
    public void shouldDeleteLike() {
        filmService.likeTheFilm(1L, 1L);
        filmService.deleteLike(1L, 1L);

        assertTrue(filmService.getAllFilms().stream()
                .filter(film -> film.getId() == 1L)
                .findFirst()
                .orElseThrow()
                .getLikesCount() == 0);
    }

    @Test
    public void shouldGetPopularFilms() {
            filmService.likeTheFilm(1L, 1L);
            filmService.likeTheFilm(1L, 2L);
            filmService.likeTheFilm(2L, 3L);
            filmService.likeTheFilm(2L, 1L);
            filmService.likeTheFilm(2L, 2L);
            filmService.likeTheFilm(3L, 1L);

        List<Film> popularFilms = (List<Film>) filmService.getPopularFilms(3);
        System.out.println(popularFilms);

        assertEquals(3, popularFilms.size());
        assertEquals(2L, popularFilms.get(0).getId());
        assertEquals(1L, popularFilms.get(1).getId());
        assertEquals(3L, popularFilms.get(2).getId());
    }
}