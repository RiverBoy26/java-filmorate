package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage") private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья!");
        }

        if (userStorage.existsFriend(userId, friendId)) {
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        userStorage.addFriend(userId, friendId);

        if (userStorage.existsFriend(friendId, userId)) {
            userStorage.updateFriendStatus(userId, friendId, true);
            userStorage.updateFriendStatus(friendId, userId, true);
        } else {
            userStorage.updateFriendStatus(userId, friendId, true);
        }
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public Optional<User> getUser(long id) {
        return userStorage.getUser(id);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void removeUser(User user) {
        userStorage.removeUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(long userId1, long userId2) {
        userStorage.getUser(userId1)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId1 + " не найден"));
        userStorage.getUser(userId2)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId2 + " не найден"));

        return userStorage.getCommonFriends(userId1, userId2);
    }

    public List<User> getUserFriends(long userId) {
        return userStorage.getFriendIds(userId);
    }
}