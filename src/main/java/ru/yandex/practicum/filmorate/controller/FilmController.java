package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {
    private final FilmService filmService;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmController(FilmService filmService, FilmStorage filmStorage) {
        this.filmService = filmService;
        this.filmStorage = filmStorage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film filmAdd(@RequestBody Film film) {
        validateDataFilm(film);
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film filmUpdate(@RequestBody @NotNull Film film) {
        validateDataFilm(film);
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> showFilms() {
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.NotFoundException(
                        "Фильм с ID " + id + " не найден"));
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getTopFilms(count);
    }

    private static final int DESCRIPTION_MAX_LENGTH = 200;

    private void validateDataFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым!");
        }

        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым!");
        }

        if (film.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
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