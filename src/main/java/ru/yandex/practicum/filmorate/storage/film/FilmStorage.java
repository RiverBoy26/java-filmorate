package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    void removeFilm(Film film);

    Collection<Film> getAllFilms();

    Optional<Film> getFilm(long id);

    Collection<Film> getTopFilms(int count);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);
}