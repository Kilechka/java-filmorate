package ru.yandex.practicum.filmorate.daoTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:schemaTest.sql")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTests {

    private final GenreService genreService;

    @Test
    public void shouldGetGenreById() {
        Genre genre = genreService.getGenreById(1);
        Genre genre1 = genreService.getGenreById(2);

        assertEquals("Комедия", genre.getName());
        assertEquals("Драма", genre1.getName());
    }

    @Test
    public void shouldGetAllGenres() {
        Collection<Genre> genres = genreService.getAllGenres();

        assertFalse(genres.isEmpty());
        assertTrue(genres.size() == 6);
    }
}
