package ru.yandex.practicum.filmorate.daoTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:schemaTest.sql")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTests {

    private final MpaService mpaService;

    @Test
    public void shouldGetMpaById() {
        Mpa mpa = mpaService.getMpaById(1);
        Mpa mpa1 = mpaService.getMpaById(2);

        assertEquals("G", mpa.getName());
        assertEquals("PG", mpa1.getName());
    }

    @Test
    public void shouldGetgetAllMpa() {
        Collection<Mpa> mpaList = mpaService.getAllMpa();
        System.out.println(mpaList.size());

        assertFalse(mpaList.isEmpty());
        assertTrue(mpaList.size() == 5);
    }
}
