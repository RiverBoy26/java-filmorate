package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);
        UserService userService = new UserService(userStorage);

        filmController = new FilmController(filmService, filmStorage);
        userController = new UserController(userService, userStorage);
    }

    @Test
    void addAndGetFilmById_shouldWorkCorrectly() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film addedFilm = filmController.filmAdd(film);
        Long filmId = addedFilm.getId();

        Film retrievedFilm = filmController.getFilmById(filmId);

        assertNotNull(retrievedFilm);
        assertEquals(filmId, retrievedFilm.getId());
        assertEquals("Test Film", retrievedFilm.getName());
        assertEquals("Test Description", retrievedFilm.getDescription());
    }

    @Test
    void getFilmById_withNonExistingId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.getFilmById(999L)
        );
        assertTrue(exception.getMessage().contains("Фильм с ID 999 не найден"));
    }

    @Test
    void addAndGetUserById_shouldWorkCorrectly() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User addedUser = userController.addUser(user);
        Long userId = addedUser.getId();

        User retrievedUser = userController.getUserById(userId);

        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getId());
        assertEquals("test@email.com", retrievedUser.getEmail());
        assertEquals("Test Name", retrievedUser.getName());
    }

    @Test
    void getUserById_withNonExistingId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.getUserById(999L)
        );
        assertTrue(exception.getMessage().contains("Пользователь с ID 999 не найден"));
    }

    @Test
    void updateFilm_withNonExistingId_shouldThrowNotFoundException() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Non-existent Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.filmUpdate(film)
        );
        assertTrue(exception.getMessage().contains("Фильм с ID 999 не найден"));
    }

    @Test
    void updateUser_withNonExistingId_shouldThrowNotFoundException() {
        User user = new User();
        user.setId(999L);
        user.setEmail("nonexistent@email.com");
        user.setLogin("nonexistent");
        user.setName("Non-existent User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.updateUser(user)
        );
        assertTrue(exception.getMessage().contains("Пользователь с ID 999 не найден"));
    }

    @Test
    void addLikeToFilm_shouldWorkCorrectly() {
        User user = new User();
        user.setEmail("user@email.com");
        user.setLogin("userlogin");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser = userController.addUser(user);

        Film film = new Film();
        film.setName("Film with like");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Film addedFilm = filmController.filmAdd(film);

        assertDoesNotThrow(() -> filmController.addLike(addedFilm.getId(), addedUser.getId()));

        Film updatedFilm = filmController.getFilmById(addedFilm.getId());
        assertTrue(updatedFilm.getLikes().contains(addedUser.getId()));
    }

    @Test
    void addAndRemoveFriend_shouldWorkCorrectly() {
        User user1 = new User();
        user1.setEmail("user1@email.com");
        user1.setLogin("user1login");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User addedUser1 = userController.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User addedUser2 = userController.addUser(user2);

        assertDoesNotThrow(() -> userController.addFriend(addedUser1.getId(), addedUser2.getId()));

        User updatedUser1 = userController.getUserById(addedUser1.getId());
        User updatedUser2 = userController.getUserById(addedUser2.getId());

        assertTrue(updatedUser1.getFriends().contains(addedUser2.getId()));
        assertTrue(updatedUser2.getFriends().contains(addedUser1.getId()));

        // Удаляем из друзей
        assertDoesNotThrow(() -> userController.removeFriend(addedUser1.getId(), addedUser2.getId()));

        // Проверяем, что дружба удалена
        updatedUser1 = userController.getUserById(addedUser1.getId());
        updatedUser2 = userController.getUserById(addedUser2.getId());

        assertFalse(updatedUser1.getFriends().contains(addedUser2.getId()));
        assertFalse(updatedUser2.getFriends().contains(addedUser1.getId()));
    }
}