package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {
	private FilmController filmController;
	private UserController userController;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
		userController = new UserController();
	}

	@Test
	void addFilm_withValidData_shouldReturnFilm() {
		Film film = new Film();
		film.setName("Film 1");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(150);

		Film result = filmController.filmAdd(film);

		assertNotNull(result);
        assertEquals("Film 1", result.getName());
	}

	@Test
	void addFilm_withEmptyDescription_shouldThrowException() {
		Film film = new Film();
		film.setName("Film 2");
		film.setDescription("");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(90);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);
		assertEquals("Описание не может быть пустым!", exception.getMessage());
	}

	@Test
	void addFilm_withNullDescription_shouldThrowException() {
		Film film = new Film();
		film.setName("Film 3");
		film.setDescription(null);
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(90);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);
		assertEquals("Описание не может быть пустым!", exception.getMessage());
	}

	@Test
	void addFilm_withDescriptionExactly200Chars_shouldSucceed() {
		String description = "A".repeat(200);
		Film film = new Film();
		film.setName("Film 4");
		film.setDescription(description);
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(90);

		assertDoesNotThrow(() -> filmController.filmAdd(film));
	}

	@Test
	void addFilm_withDescription201Chars_shouldThrowException() {
		String description = "A".repeat(201);
		Film film = new Film();
		film.setName("Film 5");
		film.setDescription(description);
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(90);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);
		assertEquals("Описание превысило 200 символов!", exception.getMessage());
	}

	@Test
	void addFilm_withReleaseDateBeforeCinemaBirth_shouldThrowException() {
		Film film = new Film();
		film.setName("Film old");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		film.setDuration(150);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);
		assertEquals("Релиз не может быть раньше даты рождения кино!", exception.getMessage());
	}

	@Test
	void addFilm_withReleaseDateExactlyCinemaBirth_shouldSucceed() {
		Film film = new Film();
		film.setName("Film first");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		film.setDuration(150);

		assertDoesNotThrow(() -> filmController.filmAdd(film));
	}

	@Test
	void addFilm_withInvalidDuration_shouldThrowException() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(-1);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);
		assertEquals("Продолжительность должна быть положительной!", exception.getMessage());
	}

	@Test
	void updateFilm_withNonExistingId_shouldNotUpdate() {
		Film film = new Film();
		film.setId(999L);
		film.setName("Film 1");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(150);

		// First add a film with ID 1
		Film initialFilm = new Film();
		initialFilm.setId(1);
		initialFilm.setName("Film 1");
		initialFilm.setDescription("Описание");
		initialFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
		initialFilm.setDuration(150);
		filmController.filmAdd(initialFilm);

		// Try to update with non-existing ID
		filmController.filmUpdate(film);

		// The non-existing film should not be added
		assertEquals(1, filmController.showFilms().size());
		assertTrue(filmController.showFilms().stream()
				.noneMatch(f -> f.getId() == 999L));
	}

	@Test
	void addUser_withValidData_shouldReturnUser() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User result = userController.addUser(user);

		assertNotNull(result);
        assertEquals("valid@email.com", result.getEmail());
	}

	@Test
	void addUser_withEmptyEmail_shouldThrowException() {
		User user = new User();
		user.setEmail("");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Почта не может быть пустой и должна содержать @", exception.getMessage());
	}

	@Test
	void addUser_withNullEmail_shouldThrowException() {
		User user = new User();
		user.setEmail(null);
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Почта не может быть пустой и должна содержать @", exception.getMessage());
	}

	@Test
	void addUser_withEmailWithoutAtSymbol_shouldThrowException() {
		User user = new User();
		user.setEmail("valid-email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Почта не может быть пустой и должна содержать @", exception.getMessage());
	}

	@Test
	void addUser_withEmptyLogin_shouldThrowException() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Логин не может быть пустым и содержать пробелы!", exception.getMessage());
	}

	@Test
	void addUser_withNullLogin_shouldThrowException() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin(null);
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Логин не может быть пустым и содержать пробелы!", exception.getMessage());
	}

	@Test
	void addUser_withLoginContainingSpaces_shouldThrowException() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("invalid login");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Логин не может быть пустым и содержать пробелы!", exception.getMessage());
	}

	@Test
	void addUser_withFutureBirthday_shouldThrowException() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.now().plusDays(1));

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> userController.addUser(user)
		);
		assertEquals("Введите действительную дату рождения!", exception.getMessage());
	}

	@Test
	void addUser_withTodayAsBirthday_shouldSucceed() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.now());

		assertDoesNotThrow(() -> userController.addUser(user));
	}

	@Test
	void addUser_withEmptyName_shouldUseLoginAsName() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User result = userController.addUser(user);

		assertEquals("validlogin", result.getName());
	}

	@Test
	void addUser_withNullName_shouldUseLoginAsName() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName(null);
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User result = userController.addUser(user);

		assertEquals("validlogin", result.getName());
	}

	@Test
	void addUser_withWhitespaceName_shouldUseLoginAsName() {
		User user = new User();
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("   ");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User result = userController.addUser(user);

		assertEquals("validlogin", result.getName());
	}

	@Test
	void updateUser_withNonExistingId_shouldReturnEmptyCollection() {
		User user = new User();
		user.setId(999L);
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User result = userController.updateUser(user);

		assertNotNull(result);
		assertEquals(999L, result.getId());
		assertEquals(0, userController.showUsers().size());
	}

	@Test
	void showFilms_whenNoFilmsAdded_shouldReturnEmptyCollection() {
		assertTrue(filmController.showFilms().isEmpty());
	}

	@Test
	void showUsers_whenNoUsersAdded_shouldReturnEmptyCollection() {
		assertTrue(userController.showUsers().isEmpty());
	}

	@Test
	void addFilm_withZeroDuration_shouldThrowException() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(0);

		ValidationException exception = assertThrows(
				ValidationException.class,
				() -> filmController.filmAdd(film)
		);

		assertEquals("Продолжительность должна быть положительной!", exception.getMessage());
	}

	@Test
	void addUser_withMinimalAge_shouldSucceed() {
		User user = new User();
		user.setId(999L);
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.now());

		assertDoesNotThrow(() -> userController.addUser(user));
	}

	@Test
	void addUser_withVeryOldBirthday_shouldSucceed() {
		User user = new User();
		user.setId(999L);
		user.setEmail("valid@email.com");
		user.setLogin("validlogin");
		user.setName("Valid Name");
		user.setBirthday(LocalDate.of(1900, 1, 1));

		assertDoesNotThrow(() -> userController.addUser(user));
	}
}
