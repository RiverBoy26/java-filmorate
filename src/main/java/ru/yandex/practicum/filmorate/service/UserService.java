package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        User user1 = userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User user2 = userStorage.getUser(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }

        if (user1.getFriends().contains(friendId)) {
            throw new AlreadyExistsException("Пользователи уже являются друзьями");
        }

        user1.getFriends().add(friendId);
        user2.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user1 = userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User user2 = userStorage.getUser(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        user1.getFriends().remove(friendId);
        user2.getFriends().remove(userId);
    }

    public List<User> getCommonFriends(long userId1, long userId2) {
        User user1 = userStorage.getUser(userId1)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId1 + " не найден"));
        User user2 = userStorage.getUser(userId2)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId2 + " не найден"));

        Set<Long> commonFriendIds = user1.getFriends().stream()
                .filter(user2.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(id -> userStorage.getUser(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(long userId) {
        User user = userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        return user.getFriends().stream()
                .map(id -> userStorage.getUser(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден")))
                .collect(Collectors.toList());
    }
}