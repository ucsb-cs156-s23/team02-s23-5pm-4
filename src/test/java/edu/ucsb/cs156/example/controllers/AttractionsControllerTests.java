package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Attractions;
import edu.ucsb.cs156.example.repositories.AttractionsRepository;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AttractionsController.class)
@Import(TestConfig.class)
public class AttractionsControllerTests extends ControllerTestCase {

        @MockBean
        AttractionsRepository attractionsRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/attractions/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/attractions/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/attractions/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/attractions?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/attractions/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/attractions/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/attractions/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                //LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Attractions attractions = Attractions.builder()
                                .name("bridge")
                                .address("123 rd")
                                .description("red")
                                .build();

                when(attractionsRepository.findById(eq(7L))).thenReturn(Optional.of(attractions));

                // act
                MvcResult response = mockMvc.perform(get("/api/attractions?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(attractionsRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(attractions);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(attractionsRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/attractions?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(attractionsRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Attractions with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_attractions() throws Exception {

                // arrange
                //LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Attractions attractions1 = Attractions.builder()
                                .name("bridge")
                                .address("123 rd")
                                .description("red")
                                .build();

                //LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Attractions attractions2 = Attractions.builder()
                                .name("building")
                                .address("456 ln")
                                .description("tall")
                                .build();

                ArrayList<Attractions> expectedAttractions = new ArrayList<>();
                expectedAttractions.addAll(Arrays.asList(attractions1, attractions2));

                when(attractionsRepository.findAll()).thenReturn(expectedAttractions);

                // act
                MvcResult response = mockMvc.perform(get("/api/attractions/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(attractionsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedAttractions);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_attractions() throws Exception {
                // arrange

                Attractions attractions1 = Attractions.builder()
                                .name("townhall")
                                .address("road")
                                .description("blue")
                                .build();

                when(attractionsRepository.save(eq(attractions1))).thenReturn(attractions1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/attractions/post?name=townhall&address=road&description=blue")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(attractionsRepository, times(1)).save(attractions1);
                String expectedJson = mapper.writeValueAsString(attractions1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_attractions() throws Exception {
                // arrange

                //LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Attractions attractions1 = Attractions.builder()
                                .name("bridge")
                                .address("123 rd")
                                .description("red")
                                .build();

                when(attractionsRepository.findById(eq(15L))).thenReturn(Optional.of(attractions1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/attractions?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(attractionsRepository, times(1)).findById(15L);
                verify(attractionsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Attractions with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_attractions_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(attractionsRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/attractions?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(attractionsRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Attractions with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_attractions() throws Exception {
                // arrange

                //LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                //LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                Attractions attractionsOrig = Attractions.builder()
                                .name("bridge")
                                .address("123 rd")
                                .description("red")
                                .build();

                Attractions attractionsEdited = Attractions.builder()
                                .name("building")
                                .address("546")
                                .description("tall")
                                .build();

                String requestBody = mapper.writeValueAsString(attractionsEdited);

                when(attractionsRepository.findById(eq(67L))).thenReturn(Optional.of(attractionsOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/attractions?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(attractionsRepository, times(1)).findById(67L);
                verify(attractionsRepository, times(1)).save(attractionsEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_attractions_that_does_not_exist() throws Exception {
                // arrange

                //LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Attractions attractionsEdited = Attractions.builder()
                                .name("bridge")
                                .address("123 rd")
                                .description("red")
                                .build();

                String requestBody = mapper.writeValueAsString(attractionsEdited);

                when(attractionsRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/attractions?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(attractionsRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Attractions with id 67 not found", json.get("message"));

        }
}
