package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private User userRowMapper(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id((long) rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    @Override
    public User addUser(User user) {
        validateUser(user);

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId((long) Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId);

        if (count == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, false)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public boolean existsFriend(long userId, long friendId) {
        String sql = """
            SELECT COUNT(*)
            FROM friends
            WHERE user_id = ? AND friend_id = ?
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count > 0;
    }

    @Override
    public void updateFriendStatus(long userId, long friendId, boolean status) {
        String sql = """
            UPDATE friends
            SET status = ?
            WHERE user_id = ? AND friend_id = ?
        """;
        jdbcTemplate.update(sql, status, userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден!");
        }

        Integer friendCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, friendId);
        if (friendCount == 0) {
            throw new NotFoundException("Пользователь с id " + friendId + " не найден!");
        }

        String checkFriendshipSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";

        String deleteSql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(deleteSql, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(long userId) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId);

        if (count == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = """
            SELECT friend_id
            FROM friends
            WHERE user_id = ? AND status = true
        """;

        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId
        ));
    }

    @Override
    public User updateUser(User user) {
        validateUser(user);

        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getId());

        if (count == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    private boolean userExists(long filmId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count > 0;
    }

    @Override
    public void removeUser(User user) {
        // Сначала удаляем связи
        String deleteFriendsSql = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFriendsSql, user.getId(), user.getId());

        // Затем пользователя
        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        int deleted = jdbcTemplate.update(deleteUserSql, user.getId());

        if (deleted == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
    }

    @Override
    public Optional<User> getUser(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::userRowMapper, id));
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::userRowMapper);
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userExists(otherId)) {
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "    SELECT f1.friend_id FROM friends f1 " +
                "    WHERE f1.user_id = ? AND f1.status = true " +
                "    INTERSECT " +
                "    SELECT f2.friend_id FROM friends f2 " +
                "    WHERE f2.user_id = ? AND f2.status = true" +
                ")";
        return jdbcTemplate.query(sql, this::userRowMapper, userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин!");
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Некорректная дата рождения");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
