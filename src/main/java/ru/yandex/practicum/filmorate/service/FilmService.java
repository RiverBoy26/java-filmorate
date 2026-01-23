package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage") private final FilmStorage filmStorage;
    @Qualifier("userDbStorage") private final UserStorage userStorage;
    private final GenreService genreService;

    public void addLike(long filmId, long userId) {
        filmStorage.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));

        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getTopFilms(int count) {
        return filmStorage.getTopFilms(count);
    }

    public Film addFilm(Film film) {
        validateMpaAndGenre(film);
        return filmStorage.addFilm(fillGenres(film));
    }

    public Film updateFilm(Film film) {
        validateMpaAndGenre(film);
        filmStorage.getFilm(film.getId())
                .orElseThrow(() -> new NotFoundException("Данный фильм не найден!"));
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void deleteFilm(Film film) {
        filmStorage.removeFilm(film);
    }

    public Optional<Film> getFilm(long id) {
        return filmStorage.getFilm(id);
    }

    private void validateMpaAndGenre(Film f) {
        var genreIds = f.getGenres() == null ? Set.<Integer>of()
                : f.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var missing = genreService.findMissingIds(genreIds);
        if (!missing.isEmpty())
            throw new NotFoundException("Жанр не найден");
    }

    private Film fillGenres(Film f) {
        if (f.getGenres() == null || f.getGenres().isEmpty()) return f;
        var filled = f.getGenres().stream()
                .filter(Objects::nonNull)
                .filter(g -> g.getId() != 0)
                .collect(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparingInt(Genre::getId))
                ));
        f.setGenres(filled);
        return f;
    }
}