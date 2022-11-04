package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void addFriend(Integer id, Integer friendId) {
        userStorage.addFriend(id, friendId);
    }

    public void deleteFriend(Integer id, Integer friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public List<User> getFriendsSet(Integer id) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        return userStorage.getFriendsSet(id);
    }

    public List<User> getMutualFriendsSet(Integer id, Integer friendId) {
        return userStorage.getMutualFriendsSet(id, friendId);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public User findUserById(Integer id) {
        return userStorage.findById(id);
    }

    public void deleteAllUsers() {
        userStorage.deleteAll();
    }

    public List<Film> getRecommendations(Integer id) {
        if (getIdUsersWithSimilarInterests(id).isEmpty()) {
            return new ArrayList<>();
        }
        Integer idUserWithClosestInterests = getIdUsersWithSimilarInterests(id).get(0);
        List<Film> recommendationsFilms = filmStorage.getRecommendations(id, idUserWithClosestInterests);
        return recommendationsFilms;

    }

    public List<Integer> getIdUsersWithSimilarInterests(int id) {
        return userStorage.getIdUsersWithSimilarInterests(id);
    }

}
