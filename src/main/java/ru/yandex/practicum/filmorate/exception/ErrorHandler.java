package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleValidationException(final ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return Map.of("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map handleNotFoundException(final NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return Map.of("Объект не найден", e.getMessage());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map handleAlreadyExistsException(final AlreadyExistsException e) {
        log.warn("Конфликт данных: {}", e.getMessage());
        return Map.of("Конфликт данных", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn("Некорректный аргумент: {}", e.getMessage());
        return Map.of("Некорректный запрос", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String errorMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("Ошибка валидации аргументов: {}", errorMessage);
        assert errorMessage != null;
        return Map.of("Ошибка валидации аргументов", errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleConstraintViolationException(final ConstraintViolationException e) {
        log.warn("Нарушение ограничений валидации: {}", e.getMessage());
        return Map.of("Нарушение ограничений валидации", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("Некорректный тип параметра '%s': ожидался %s, получено '%s'",
                e.getName(), e.getRequiredType(), e.getValue());
        log.warn(errorMessage);
        return Map.of("Некорректный тип параметра", errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("Некорректный формат JSON: {}", e.getMessage());
        return Map.of("Некорректный формат JSON", "Проверьте правильность структуры запроса");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map handleException(final Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return Map.of("Внутренняя ошибка сервера", "Произошла непредвиденная ошибка");
    }
}
