package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film filmAdd(@RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film filmUpdate(@RequestBody @NotNull Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> showFilms() {
        return filmService.getAllFilms();
    }

    @DeleteMapping
    public void deleteFilm(Film film) {
        filmService.deleteFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        return filmService.getFilm(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.NotFoundException(
                        "Фильм с ID " + id + " не найден"));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getTopFilms(count);
    }
}