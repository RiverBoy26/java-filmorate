package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final int DESCRIPTION_MAX_LENGTH = 200;

    private static final LocalDate BIRTHDAY_CINEMA = LocalDate.of(1895, 12,28);

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa_id) VALUES (?,?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != 0) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setObject(5, null);
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        replaceFilmGenres(film.getId(), film.getGenres());
        return getFilm(film.getId()).orElseThrow();
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilm(film);
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден!");
        }

        replaceFilmGenres(film.getId(), film.getGenres());
        return getFilm(film.getId()).orElseThrow();
    }

    @Override
    public void removeFilm(Film film) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    @Override
    public Optional<Film> getFilm(long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToFilm(rs), id));
    }

    @Override
    public Collection<Film> getTopFilms(int count) {
        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), count);
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film film = Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .build();

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            String mpaSql = "SELECT * FROM mpa WHERE id = ?";
            Mpa mpa = jdbcTemplate.queryForObject(mpaSql,
                    (rsMpa, rowNum) -> Mpa.builder()
                            .id(rsMpa.getInt("id"))
                            .name(rsMpa.getString("name"))
                            .description(rsMpa.getString("description"))
                            .build(),
                    mpaId);
            film.setMpa(mpa);
        }

        String genresSql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        Set<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(genresSql,
                (rsGenre, rowNum) -> Genre.builder()
                        .id(rsGenre.getInt("id"))
                        .name(rsGenre.getString("name"))
                        .build(),
                film.getId()));
        film.setGenres(genres);

        return film;
    }

    public void addLike(long filmId, long userId) {
        if (isLikeExists(filmId, userId)) {
            throw new NotFoundException("Пользователь ставил лайк этому фильму");
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private boolean isLikeExists(long filmId, long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count > 0;
    }

    public void removeLike(long filmId, long userId) {
        if (!isLikeExists(filmId, userId)) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private void replaceFilmGenres(long filmId, Set<Genre> genres) {
        // Удаляем все старые жанры
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        // Если нет новых жанров - выходим
        if (genres == null || genres.isEmpty()) {
            return;
        }

        // Добавляем новые жанры по одному
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            if (genre != null && genre.getId() > 0) {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
            }
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма отсутствует!");
        }
        if (film.getDescription() == null || film.getDescription().isBlank() || film.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
            throw new ValidationException("Описание фильма отсутствует или слишком велико!");
        }
        if (film.getReleaseDate().isBefore(BIRTHDAY_CINEMA)) {
            throw new ValidationException("Некорректная дата релиза!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной!");
        }

        if (film.getMpa() == null) {
            throw new ValidationException("Фильм должен иметь возрастной рейтинг!");
        }

        if (film.getMpa().getId() != 0) {
            String countSql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, film.getMpa().getId());
            if (count == 0) {
                throw new NotFoundException("Возрастной рейтинг с id " + film.getMpa().getId() + " не найден!");
            }
        }
    }
}
