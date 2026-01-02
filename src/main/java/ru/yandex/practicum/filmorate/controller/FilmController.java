package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film filmAdd(@RequestBody @Valid Film film) {
        validateDataFilm(film);
        film.setId(newFilmId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film filmUpdate(@RequestBody @NotNull @Valid Film film) {
        validateDataFilm(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        } else {
            throw new ValidationException("ID фильма не найден!");
        }
    }

    @GetMapping
    public Collection<Film> showFilms() {
        return films.values();
    }

    private long newFilmId() {
        long currentFilmId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentFilmId;
    }

    private void validateDataFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым!");
        }

        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым!");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание превысило 200 символов!");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Релиз не может быть раньше даты рождения кино!");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительной!");
        }
    }
}
