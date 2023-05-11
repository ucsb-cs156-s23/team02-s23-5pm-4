package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.repositories.MovieRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

//import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MoviesController.class)
@Import(TestConfig.class)
public class MoviesControllerTests extends ControllerTestCase {

        @MockBean
        MovieRepository movieRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/movies/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/movies?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/movies/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                // LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Movie movie = Movie.builder()
                                .name("moviename")
                                .genre("comedy")
                                .year(2022)
                                .build();

                when(movieRepository.findById(eq(7L))).thenReturn(Optional.of(movie));

                // act
                MvcResult response = mockMvc.perform(get("/api/movie?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(movieRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(movie);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(movieRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(movieRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Movie with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_movies() throws Exception {

                // arrange
                // LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Movie movie1 = Movie.builder()
                                .name("firstDayOfClasses")
                                .genre("horror")
                                .year(2023)
                                .build();

                // LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Movie movie2 = Movie.builder()
                                .name("lastDayOfClasses")
                                .genre("horror")
                                .year(2023)
                                .build();

                ArrayList<Movie> expectedMovies = new ArrayList<>();
                expectedMovies.addAll(Arrays.asList(movie1, movie2));

                when(movieRepository.findAll()).thenReturn(expectedMovies);

                // act
                MvcResult response = mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(movieRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMovies);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_movie() throws Exception {
                // arrange

                // LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Movie movie1 = Movie.builder()
                                .name("firstDayOfClasses")
                                .genre("romance")
                                .year(2024)
                                .build();

                when(movieRepository.save(eq(movie1))).thenReturn(movie1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/movies/post?name=firstDayOfClasses&genre=romance&year=2024")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).save(movie1);
                String expectedJson = mapper.writeValueAsString(movie1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_movie() throws Exception {
                // arrange

                // LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Movie movie1 = Movie.builder()
                                .name("firstDayOfClasses")
                                .genre("horror")
                                .year(2025)
                                .build();

                when(movieRepository.findById(eq(15L))).thenReturn(Optional.of(movie1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(15L);
                verify(movieRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_movie_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(movieRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_movie() throws Exception {
                // arrange

                // LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                // LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                Movie movieOrig = Movie.builder()
                                .name("firstDayOfClasses")
                                .genre("action")
                                .year(2026)
                                .build();

                Movie movieEdited = Movie.builder()
                                .name("firstDayOfFestivus")
                                .genre("action")
                                .year(2026)
                                .build();

                String requestBody = mapper.writeValueAsString(movieEdited);

                when(movieRepository.findById(eq(67L))).thenReturn(Optional.of(movieOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(67L);
                verify(movieRepository, times(1)).save(movieEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_movie_that_does_not_exist() throws Exception {
                // arrange

                // LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Movie editedMovie = Movie.builder()
                                .name("firstDayOfClasses")
                                .genre("comedy")
                                .year(2021)
                                .build();

                String requestBody = mapper.writeValueAsString(editedMovie);

                when(movieRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(movieRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 67 not found", json.get("message"));

        }
}
