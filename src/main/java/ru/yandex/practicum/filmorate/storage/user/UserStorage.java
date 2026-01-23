package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    void removeUser(User user);

    Optional<User> getUser(long id);

    Collection<User> getAllUsers();

    void addFriend(long userId, long friendId);

    boolean existsFriend(long userId, long friendId);

    void updateFriendStatus(long userId, long friendId, boolean status);

    void removeFriend(long userId, long friendId);

    List<User> getFriendIds(long userId);

    Collection<User> getCommonFriends(long userId, long otherId);
}