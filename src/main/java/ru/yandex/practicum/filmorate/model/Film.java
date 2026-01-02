package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private String duration;
}
