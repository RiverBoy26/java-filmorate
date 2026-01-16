package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    @Qualifier("mpaDbStorage") private final MpaStorage mpaStorage;

    public List<Mpa> getAllMpa() {
        return mpaStorage.findAllMpa();
    }

    public Mpa getMpaById(int id) {
        return mpaStorage.findMpaById(id)
                .orElseThrow(() -> new NotFoundException("Возрастная категория не найдена!"));
    }
}
