package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> findAllGenres();

    Optional<Genre> findGenreById(int id);

    Set<Integer> findMissingIds(Set<Integer> ids);
}
