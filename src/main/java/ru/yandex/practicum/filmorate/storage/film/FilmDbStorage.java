package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.util.*;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAll() {
        String query = "SELECT * FROM films";

        List<Film> films = jdbcTemplate.query(query, FilmMapper::mapToFilm);
        films.forEach(film -> {
                    String likesQuery = "SELECT user_id FROM films_likes WHERE film_id = ?";
                    List<Integer> likes = jdbcTemplate.queryForList(likesQuery, Integer.class, film.getId());
                    film.setLikes(new HashSet<>(likes));

                    String mpaQuery = "SELECT name FROM mpa_rating where mpa_rate_id = ?";
                    String mpaRate = jdbcTemplate.queryForObject(mpaQuery, String.class, film.getMpaRateId());
                    film.setMpaRatingName(mpaRate);
                });
        return films;
    }

    @Override
    public Film findById(Integer id) {
        try {
            String filmQuery = "SELECT * FROM films WHERE id=?";
            Film film = jdbcTemplate.queryForObject(filmQuery, FilmMapper::mapToFilm, id);

            String likesQuery = "SELECT user_id FROM films_likes WHERE film_id = ?";
            Optional<Set<Integer>> optionalList = Optional.of(new HashSet<>(jdbcTemplate.queryForList(likesQuery, Integer.class, id)));
            film.setLikes(optionalList.orElse(new HashSet<>(0)));

            String mpaQuery = "SELECT name FROM mpa_rating where mpa_rate_id = ?";
            String mpaRate = jdbcTemplate.queryForObject(mpaQuery, String.class, film.getMpaRateId());
            film.setMpaRatingName(mpaRate);

            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("id", String.format("user with id %d not found", id));
        }
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int id = jdbcInsert.executeAndReturnKey(new BeanPropertySqlParameterSource(film)).intValue();

        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }
        if (this.findById(film.getId()) == null) {
            throw new NotFoundException("id", String.format("film with id=%d not found", film.getId()));
        }
        String query = "update films set " +
                "title = ?, description = ?, release_date = ?, duration = ?, rating = ?, mpa_rate_id = ?" +
                "where id = ?";
        jdbcTemplate.update(query, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating(),
                film.getMpaRateId());

        return findById(film.getId());
    }

    @Override
    public void deleteAll() {
        String query = "delete from films";
        jdbcTemplate.update(query);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        try {
            String query = "insert into films_likes(film_id, user_id) " +
                    "values (?, ?)";
            jdbcTemplate.update(query, filmId, userId);
        } catch (DataAccessException e) {
            e.getMessage();
        }

    }

    @Override
    public void deleteLike(Integer filmId, Integer userId) {
        String query = "delete from films_likes where film_id = ? and user_id = ?";

        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public List<Film> getFilmsTop(Integer count) {
        List<Film> filmsSorted = new ArrayList<>();
        try {
            String query = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING, f.MPA_RATE_ID\n" +
                    "FROM FILMS f \n" +
                    "LEFT JOIN FILMS_LIKES fl ON f.ID = fl.FILM_ID\n" +
                    "GROUP BY f.ID\n" +
                    "ORDER BY count(fl.FILM_ID) DESC\n" +
                    "LIMIT ?";
            filmsSorted = jdbcTemplate.query(query, FilmMapper::mapToFilm, count);
        } catch (DataAccessException e) {
            e.getMessage();
        }
        return filmsSorted;
    }
}