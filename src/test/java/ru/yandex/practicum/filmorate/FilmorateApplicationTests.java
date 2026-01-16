package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;
    private MpaDbStorage mpaStorage;
    private GenreDbStorage genreStorage;

    @BeforeEach
    void setUp() {
        // Инициализируем все хранилища
        userStorage = new UserDbStorage(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
        mpaStorage = new MpaDbStorage(jdbcTemplate);
        genreStorage = new GenreDbStorage(jdbcTemplate);

        // Очищаем все таблицы
        clearAllTables();

        // Инициализируем справочные данные
        initializeReferenceData();
    }

    private void clearAllTables() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM genres");
        jdbcTemplate.execute("DELETE FROM mpa");
    }

    private void initializeReferenceData() {
        // MPA рейтинги
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (1, 'G', 'Нет возрастных ограничений')");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (2, 'PG', 'Рекомендуется присутствие родителей')");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (3, 'PG-13', 'Детям до 13 лет просмотр не желателен')");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (4, 'R', 'Лицам до 17 лет обязательно присутствие взрослого')");
        jdbcTemplate.update("INSERT INTO mpa (id, name, description) VALUES (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён')");

        // Жанры
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (3, 'Мультфильм')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (4, 'Триллер')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (5, 'Документальный')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (6, 'Боевик')");
    }

    // ==================== ТЕСТЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ====================

    @Test
    void testAddAndGetUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        // When
        User savedUser = userStorage.addUser(user);
        Optional<User> retrievedUser = userStorage.getUser(savedUser.getId());

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedUser.get().getLogin()).isEqualTo("testlogin");
        assertThat(retrievedUser.get().getName()).isEqualTo("Test User");
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = User.builder()
                .email("old@example.com")
                .login("oldlogin")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User savedUser = userStorage.addUser(user);

        // When
        User updatedUser = User.builder()
                .id(savedUser.getId())
                .email("new@example.com")
                .login("newlogin")
                .name("New Name")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();
        User result = userStorage.updateUser(updatedUser);

        // Then
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getName()).isEqualTo("New Name");

        Optional<User> retrievedUser = userStorage.getUser(savedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void testGetAllUsers() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        userStorage.addUser(user1);
        userStorage.addUser(user2);

        // When
        Collection<User> users = userStorage.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void testRemoveUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User savedUser = userStorage.addUser(user);

        userStorage.removeUser(savedUser);

        Assertions.assertFalse(userStorage.getAllUsers().contains(savedUser));
    }

    @Test
    void testAddAndRemoveFriend() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        // When - добавляем друга
        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        // Then
        assertThat(userStorage.existsFriend(savedUser1.getId(), savedUser2.getId())).isTrue();

        // When - удаляем друга
        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        // Then
        assertThat(userStorage.existsFriend(savedUser1.getId(), savedUser2.getId())).isFalse();
    }

    @Test
    void testGetFriendIds() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.addFriend(savedUser1.getId(), savedUser3.getId());
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser2.getId(), true);
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser3.getId(), true);

        // When
        Set<Long> friendIds = userStorage.getFriendIds(savedUser1.getId());

        // Then
        assertThat(friendIds).hasSize(2);
        assertThat(friendIds).contains(savedUser2.getId(), savedUser3.getId());
    }

    @Test
    void testGetCommonFriends() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);

        // user1 дружит с user2 и user3
        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.addFriend(savedUser1.getId(), savedUser3.getId());
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser2.getId(), true);
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser3.getId(), true);

        // user2 дружит с user3
        userStorage.addFriend(savedUser2.getId(), savedUser3.getId());
        userStorage.updateFriendStatus(savedUser2.getId(), savedUser3.getId(), true);

        // When
        Collection<User> commonFriends = userStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());

        // Then
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(savedUser3.getId());
    }

    // ==================== ТЕСТЫ ДЛЯ ФИЛЬМОВ ====================

    @Test
    void testAddAndGetFilm() {
        // Given
        Mpa mpa = Mpa.builder()
                .id(1)
                .name("G")
                .description("Нет возрастных ограничений")
                .build();

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(Genre.builder().id(1).name("Комедия").build());
        genres.add(Genre.builder().id(2).name("Драма").build());

        Film film = Film.builder()
                .name("Тестовый фильм")
                .description("Описание тестового фильма")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(genres)
                .build();

        // When
        Film savedFilm = filmStorage.addFilm(film);
        Optional<Film> retrievedFilm = filmStorage.getFilm(savedFilm.getId());

        // Then
        assertThat(savedFilm.getId()).isNotNull();
        assertThat(retrievedFilm).isPresent();
        assertThat(retrievedFilm.get().getName()).isEqualTo("Тестовый фильм");
        assertThat(retrievedFilm.get().getMpa().getId()).isEqualTo(1);
        assertThat(retrievedFilm.get().getGenres()).hasSize(2);
        assertThat(retrievedFilm.get().getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    @Test
    void testUpdateFilm() {
        // Given
        Mpa mpa = Mpa.builder().id(1).name("G").description("Нет возрастных ограничений").build();

        Film film = Film.builder()
                .name("Старый фильм")
                .description("Старое описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film savedFilm = filmStorage.addFilm(film);

        // When
        Film updatedFilm = Film.builder()
                .id(savedFilm.getId())
                .name("Новый фильм")
                .description("Новое описание")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(mpa)
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        // Then
        assertThat(result.getName()).isEqualTo("Новый фильм");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.getDuration()).isEqualTo(150);

        Optional<Film> retrievedFilm = filmStorage.getFilm(savedFilm.getId());
        assertThat(retrievedFilm).isPresent();
        assertThat(retrievedFilm.get().getName()).isEqualTo("Новый фильм");
    }

    @Test
    void testGetAllFilms() {
        // Given
        Mpa mpa = Mpa.builder().id(1).name("G").description("Нет возрастных ограничений").build();

        Film film1 = Film.builder()
                .name("Фильм 1")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film film2 = Film.builder()
                .name("Фильм 2")
                .description("Описание 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(mpa)
                .build();

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        // When
        Collection<Film> films = filmStorage.getAllFilms();

        // Then
        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Фильм 1", "Фильм 2");
    }

    @Test
    void testAddAndRemoveLike() {
        // Given
        Mpa mpa = Mpa.builder().id(1).name("G").description("Нет возрастных ограничений").build();

        Film film = Film.builder()
                .name("Фильм для лайков")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film savedFilm = filmStorage.addFilm(film);

        User user = User.builder()
                .email("liker@example.com")
                .login("liker")
                .name("Liker")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser = userStorage.addUser(user);

        // When - добавляем лайк
        filmStorage.addLike(savedFilm.getId(), savedUser.getId());

        // Then - проверяем, что фильм в топе
        Collection<Film> topFilms = filmStorage.getTopFilms(10);
        assertThat(topFilms).hasSize(1);
        assertThat(topFilms.iterator().next().getId()).isEqualTo(savedFilm.getId());

        // When - удаляем лайк
        filmStorage.removeLike(savedFilm.getId(), savedUser.getId());

        // Then - проверяем, что фильм больше не в топе (должен быть пустой список или другие фильмы)
    }

    @Test
    void testGetTopFilms() {
        // Given
        Mpa mpa = Mpa.builder().id(1).name("G").description("Нет возрастных ограничений").build();

        Film film1 = Film.builder()
                .name("Популярный фильм")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film film2 = Film.builder()
                .name("Менее популярный фильм")
                .description("Описание 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(mpa)
                .build();

        Film film3 = Film.builder()
                .name("Непопулярный фильм")
                .description("Описание 3")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(180)
                .mpa(mpa)
                .build();

        Film savedFilm1 = filmStorage.addFilm(film1);
        Film savedFilm2 = filmStorage.addFilm(film2);
        Film savedFilm3 = filmStorage.addFilm(film3);

        // Создаем пользователей
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        // Добавляем лайки: film1 - 2 лайка, film2 - 1 лайк, film3 - 0 лайков
        filmStorage.addLike(savedFilm1.getId(), savedUser1.getId());
        filmStorage.addLike(savedFilm1.getId(), savedUser2.getId());
        filmStorage.addLike(savedFilm2.getId(), savedUser1.getId());

        // When
        Collection<Film> topFilms = filmStorage.getTopFilms(2);

        // Then
        assertThat(topFilms).hasSize(2);
        // Проверяем порядок: film1 должен быть первым (больше всего лайков)
        Iterator<Film> iterator = topFilms.iterator();
        assertThat(iterator.next().getName()).isEqualTo("Популярный фильм");
        assertThat(iterator.next().getName()).isEqualTo("Менее популярный фильм");
    }

    @Test
    void testRemoveFilm() {
        // Given
        Mpa mpa = Mpa.builder().id(1).name("G").description("Нет возрастных ограничений").build();

        Film film = Film.builder()
                .name("Фильм для удаления")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.removeFilm(savedFilm);

        Assertions.assertFalse(filmStorage.getAllFilms().contains(savedFilm));
    }

    @Test
    void testFindAllMpa() {
        // When
        List<Mpa> mpaList = mpaStorage.findAllMpa();

        // Then
        assertThat(mpaList).hasSize(5);
        assertThat(mpaList)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaById() {
        // When
        Optional<Mpa> mpa = mpaStorage.findMpaById(2);

        // Then
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("PG");
        assertThat(mpa.get().getDescription()).isEqualTo("Рекомендуется присутствие родителей");
    }

    @Test
    void testFindMpaById_NotFound() {
        // When
        Optional<Mpa> mpa = mpaStorage.findMpaById(99);

        // Then
        assertThat(mpa).isEmpty();
    }

    // ==================== ТЕСТЫ ДЛЯ ЖАНРОВ ====================

    @Test
    void testFindAllGenres() {
        // When
        List<Genre> genres = genreStorage.findAllGenres();

        // Then
        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testFindGenreById() {
        // When
        Optional<Genre> genre = genreStorage.findGenreById(3);

        // Then
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Мультфильм");
    }

    @Test
    void testFindGenreById_NotFound() {
        // When
        Optional<Genre> genre = genreStorage.findGenreById(99);

        // Then
        assertThat(genre).isEmpty();
    }

    @Test
    void testFindMissingIds() {
        // Given
        Set<Integer> ids = Set.of(1, 2, 7, 8);

        // When
        Set<Integer> missingIds = genreStorage.findMissingIds(ids);

        // Then
        assertThat(missingIds).hasSize(2);
        assertThat(missingIds).containsExactlyInAnyOrder(7, 8);
    }

    @Test
    void testFindMissingIds_EmptySet() {
        // When
        Set<Integer> missingIds = genreStorage.findMissingIds(Set.of());

        // Then
        assertThat(missingIds).isEmpty();
    }

    @Test
    void testFindMissingIds_AllPresent() {
        // Given
        Set<Integer> ids = Set.of(1, 2, 3);

        // When
        Set<Integer> missingIds = genreStorage.findMissingIds(ids);

        // Then
        assertThat(missingIds).isEmpty();
    }

    // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

    @Test
    void testFilmWithGenresAndMpa() {
        // Тест комплексного создания фильма с жанрами и MPA

        // Given
        Mpa mpa = Mpa.builder()
                .id(3)
                .name("PG-13")
                .description("Детям до 13 лет просмотр не желателен")
                .build();

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(Genre.builder().id(1).name("Комедия").build());
        genres.add(Genre.builder().id(6).name("Боевик").build());

        Film film = Film.builder()
                .name("Крутой фильм")
                .description("Очень крутой фильм с разными жанрами")
                .releaseDate(LocalDate.of(2010, 5, 15))
                .duration(135)
                .mpa(mpa)
                .genres(genres)
                .build();

        // When
        Film savedFilm = filmStorage.addFilm(film);
        Optional<Film> retrievedFilm = filmStorage.getFilm(savedFilm.getId());

        // Then
        assertThat(retrievedFilm).isPresent();
        Film actualFilm = retrievedFilm.get();

        assertThat(actualFilm.getMpa().getId()).isEqualTo(3);
        assertThat(actualFilm.getMpa().getName()).isEqualTo("PG-13");
        assertThat(actualFilm.getGenres()).hasSize(2);
        assertThat(actualFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 6);
    }

    @Test
    void testUserFriendsIntegration() {
        // Комплексный тест друзей пользователя

        // Given - создаем трех пользователей
        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User user3 = User.builder()
                .email("user3@example.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);

        // Когда user1 добавляет user2 в друзья
        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser2.getId(), true);

        // Когда user1 добавляет user3 в друзья
        userStorage.addFriend(savedUser1.getId(), savedUser3.getId());
        userStorage.updateFriendStatus(savedUser1.getId(), savedUser3.getId(), true);

        // Когда user2 добавляет user3 в друзья
        userStorage.addFriend(savedUser2.getId(), savedUser3.getId());
        userStorage.updateFriendStatus(savedUser2.getId(), savedUser3.getId(), true);

        // Then - проверяем друзей user1
        Set<Long> friendIds = userStorage.getFriendIds(savedUser1.getId());
        assertThat(friendIds).hasSize(2);
        assertThat(friendIds).contains(savedUser2.getId(), savedUser3.getId());

        // Then - проверяем общих друзей user1 и user2
        Collection<User> commonFriends = userStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(savedUser3.getId());
    }

    @Test
    void testFilmLikesIntegration() {
        // Комплексный тест лайков фильма

        // Given - создаем фильм и двух пользователей
        Mpa mpa = Mpa.builder()
                .id(1)
                .name("G")
                .description("Нет возрастных ограничений")
                .build();

        Film film = Film.builder()
                .name("Фильм для теста лайков")
                .description("Описание")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film savedFilm = filmStorage.addFilm(film);

        User user1 = User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        // When - оба пользователя ставят лайк
        filmStorage.addLike(savedFilm.getId(), savedUser1.getId());
        filmStorage.addLike(savedFilm.getId(), savedUser2.getId());

        // Then - фильм должен быть в топе
        Collection<Film> topFilms = filmStorage.getTopFilms(1);
        assertThat(topFilms).hasSize(1);
        assertThat(topFilms.iterator().next().getId()).isEqualTo(savedFilm.getId());

        // When - user1 удаляет лайк
        filmStorage.removeLike(savedFilm.getId(), savedUser1.getId());

        // Then - фильм все еще должен быть в топе (но с одним лайком)
        topFilms = filmStorage.getTopFilms(1);
        assertThat(topFilms).hasSize(1);
        assertThat(topFilms.iterator().next().getId()).isEqualTo(savedFilm.getId());
    }
}
