package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controllers.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    FilmController fc;

    @Test
    void creates_newFilm_andStatusIs200() throws Exception {
        Film f = Film.builder()
                .name("New film")
                .description("Desc of new film")
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();


        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(f))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(200));
    }

    @Test
    void updates_presentedFilm_andStatusIs200() throws Exception {
        Film f = Film.builder()
                .name("New film")
                .description("Desc of new film")
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();
        Film f2 = Film.builder()
                .id(1)
                .name("New film upd")
                .description("Desc of new film")
                .releaseDate(LocalDate.now())
                .duration(25)
                .build();

        mockMvc.perform(
                put("/films")
                        .content(objectMapper.writeValueAsString(f2))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(200));

        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(f2))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().is(200))
                .andExpect(content().json(objectMapper.writeValueAsString(f2)));
    }

    @Test
    void when_Films_nameIsEmpty_andStatusIs400() throws Exception {
        Film f = Film.builder()
                .description("Desc of new film")
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();

        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(400));
    }

    @Test
    void when_FilmsDescription_lengthIsAbove200_andStatusIs400() throws Exception {
        Film f = Film.builder()
                .name("Test film")
                .description("l".repeat(250))
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();

        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(400));
    }

    @Test
    void when_FilmsReleaseDate_isBeforeCinemaBirthday_andStatusIs400() throws Exception {
        Film f = Film.builder()
                .name("Test film")
                .description("desc")
                .releaseDate(LocalDate.of(1800, Month.DECEMBER, 1))
                .duration(50)
                .build();

        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(400));
    }

    @Test
    void when_FilmsDuration_isNegative_andStatusIs400() throws Exception {
        Film f = Film.builder()
                .name("Test film")
                .description("desc")
                .releaseDate(LocalDate.now())
                .duration(-50)
                .build();

        mockMvc.perform(
                put("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(400));
    }

    @Test
    void when_FilmsId_isNegative_andStatusIs400() throws Exception {
        Film f = Film.builder()
                .id(-1)
                .name("Test film")
                .description("test desc")
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();

        mockMvc.perform(
                put("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(400));
    }

    @Test
    void films_ListIsNotEmpty_andStatusIs200() throws Exception {
        Film f = Film.builder()
                .name("Test film")
                .description("test desc")
                .releaseDate(LocalDate.now())
                .duration(50)
                .build();

        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(f))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(200));

        mockMvc.perform(
                        get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Optional.of(fc.getFilms()))));

    }

}
