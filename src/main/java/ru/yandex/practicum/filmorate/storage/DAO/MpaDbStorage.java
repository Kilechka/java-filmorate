package ru.yandex.practicum.filmorate.storage.DAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String GET_MPA_QUERY = "SELECT * FROM mpa";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM mpa WHERE mpa_id = ?";

    @Override
    public Collection<Mpa> getAllMpa() {
        return jdbcTemplate.query(GET_MPA_QUERY, this::makeMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        final List<Mpa> mpaList = jdbcTemplate.query(GET_BY_ID_QUERY, this::makeMpa, id);
        if (mpaList.isEmpty()) {
            throw new NotFoundException("MPA райтинг с id = " + id + " не найден");
        }
        log.info("Получен рейтинг с id = " + id);
        return mpaList.get(0);
    }

    public Mpa makeMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }
}