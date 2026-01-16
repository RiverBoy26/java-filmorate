package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Qualifier("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> {
        return Mpa.builder().
                id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description")).build();
    };

    @Override
    public List<Mpa> findAllMpa() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY id", mpaRowMapper);
    }

    @Override
    public Optional<Mpa> findMpaById(int id) {
        return jdbcTemplate.query("SELECT * FROM mpa WHERE id = ?", mpaRowMapper, id)
                .stream().findFirst();
    }
}
