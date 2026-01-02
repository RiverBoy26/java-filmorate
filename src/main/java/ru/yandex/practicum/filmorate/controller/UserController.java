package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody @Valid User user) {
        validateDataUser(user);
        user.setId(newUserId());
        users.put(user.getId(), user);
        return user;
    }

    @PatchMapping
    public User updateUser(@RequestBody @NotNull @Valid User user) {
        validateDataUser(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        }
        return user;
    }

    @GetMapping
    public Collection<User> showUsers() {
        return users.values();
    }

    private long newUserId() {
        long currentUserId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentUserId;
    }

    private void validateDataUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Почта не может быть пустой и должна содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Введите действительную дату рождения!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
