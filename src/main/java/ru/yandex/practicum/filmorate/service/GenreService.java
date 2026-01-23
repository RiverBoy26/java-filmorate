package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenreService {
    @Qualifier("genreDbStorage") private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre getGenreById(int id) {
        return genreStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр не найден!"));
    }

    public Set<Integer> findMissingIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        return genreStorage.findMissingIds(ids);
    }
}
