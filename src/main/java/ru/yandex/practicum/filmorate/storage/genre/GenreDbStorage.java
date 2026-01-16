package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("genreDbStorage")
public class GenreDbStorage implements GenreStorage{
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name")).build();
    };

    @Override
    public List<Genre> findAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY id", genreRowMapper);
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        return jdbcTemplate.query("SELECT * FROM genres WHERE id = ?", genreRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public Set<Integer> findMissingIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Integer> present = jdbcTemplate.queryForList(
                "SELECT id FROM genres WHERE id IN (" + placeholders + ")",
                Integer.class, ids.toArray());
        HashSet<Integer> missing = new HashSet<>(ids);
        missing.removeAll(present);
        return missing;
    }
}
