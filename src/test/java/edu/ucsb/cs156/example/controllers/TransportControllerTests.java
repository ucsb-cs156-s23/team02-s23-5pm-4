package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Transport;
import edu.ucsb.cs156.example.repositories.TransportRepository;

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


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TransportController.class)
@Import(TestConfig.class)
public class TransportControllerTests extends ControllerTestCase {

        @MockBean
        TransportRepository transportRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/transport/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/transport/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/transport/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/transport?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/transport/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/transport/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/transport/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Transport transport = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                when(transportRepository.findById(eq(7L))).thenReturn(Optional.of(transport));

                // act
                MvcResult response = mockMvc.perform(get("/api/transport?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(transportRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(transport);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(transportRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/transport?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(transportRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Transport with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_transports() throws Exception {

                // arrange

                Transport transport1 = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();


                Transport transport2 = Transport.builder()
                                .name("Standard Bike")
                                .mode("Bike")
                                .cost("100")
                                .build();

                ArrayList<Transport> expectedTransports = new ArrayList<>();
                expectedTransports.addAll(Arrays.asList(transport1, transport2));

                when(transportRepository.findAll()).thenReturn(expectedTransports);

                // act
                MvcResult response = mockMvc.perform(get("/api/transport/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(transportRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedTransports);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_transport() throws Exception {
                // arrange


                Transport transport1 = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                when(transportRepository.save(eq(transport1))).thenReturn(transport1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/transport/post?name=Standard Kart&mode=Kart&cost=1000")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).save(transport1);
                String expectedJson = mapper.writeValueAsString(transport1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange


                Transport transport1 = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                when(transportRepository.findById(eq(15L))).thenReturn(Optional.of(transport1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/transport?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById(15L);
                verify(transportRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_transport_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(transportRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/transport?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_transport() throws Exception {
                // arrange


                Transport transportOrig = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                Transport transportEdited = Transport.builder()
                                .name("Inkstriker")
                                .mode("Bike")
                                .cost("10000")
                                .build();

                String requestBody = mapper.writeValueAsString(transportEdited);

                when(transportRepository.findById(eq(67L))).thenReturn(Optional.of(transportOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/transport?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById(67L);
                verify(transportRepository, times(1)).save(transportEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_transport_that_does_not_exist() throws Exception {
                // arrange


                Transport editedKart = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                String requestBody = mapper.writeValueAsString(editedKart);

                when(transportRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/transport?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id 67 not found", json.get("message"));

        }
}
