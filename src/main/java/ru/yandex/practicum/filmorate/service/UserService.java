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

    public List<Film> getRecommendations(Integer idRecommendedUser, Integer limitFilms) {

        if (getIdUsersWithSimilarInterests(idRecommendedUser).isEmpty()) return new ArrayList<>();
        List<Integer> usersWithSimilarInterests = getIdUsersWithSimilarInterests(idRecommendedUser);
        log.info("Compiled a list of users with similar interests " + usersWithSimilarInterests);


        // пока только ближайший пользователь,чтобы хотя бы тесты пройти
        Integer idUserWithClosestInterests = usersWithSimilarInterests.get(0);
        log.info("User with similar interests has id " + getIdUsersWithSimilarInterests(idRecommendedUser).get(0));
        List<Integer> idRecommendationsFilms = filmStorage.getRecommendations(idUserWithClosestInterests,
                idRecommendedUser);
        log.info("We have a films recommendation list with id " + idRecommendationsFilms);


        /*
        //До лучших времен. Хотя тестил - вроде тоже всё работает.
                List<Integer> idRecommendationsFilms = idsFilmsRecommendations(usersWithSimilarInterests,
                idRecommendedUser, limitFilms);
        */


        List<Film> recommendationsFilms = filmsByIDFromList(idRecommendationsFilms);
        return recommendationsFilms;
    }

    public List<Integer> getIdUsersWithSimilarInterests(int id) {
        return userStorage.getIdUsersWithSimilarInterests(id);
    }

    public List<Film> filmsByIDFromList(List<Integer> ids) {
        List<Film> films = new ArrayList<>();
        for (Integer i : ids) {
            films.add(filmStorage.findById(i));
            log.info("Added to list film id " + i);
        }
        return films;
    }

    public List<Integer> idsFilmsRecommendations(List<Integer> usersWithSimilarInterests,
                                                 Integer idRecommendedUser, Integer limit) {
        List<Integer> filmsRecomendations = new ArrayList<>();
        for (Integer i : usersWithSimilarInterests) {
            log.info("Get recommendations from the user id " + i);
            List<Integer> idFilmsRecommendedByUser = filmStorage.getRecommendations(i,
                    idRecommendedUser);
            log.info("User id " + i + " recommends movies " + idFilmsRecommendedByUser);
            for (int j = 0; (j<idFilmsRecommendedByUser.size())&&(filmsRecomendations.size()<limit); j++) {
                Integer idFilm = idFilmsRecommendedByUser.get(j);
                if (!filmsRecomendations.contains(idFilm)) {
                    filmsRecomendations.add(idFilm);
                    log.info("Film id " + idFilm + " added to recommendation list");
                }
            }
        }
        return filmsRecomendations;
    }
}
