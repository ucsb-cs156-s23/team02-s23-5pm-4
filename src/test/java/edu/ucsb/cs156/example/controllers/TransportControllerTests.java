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
                mockMvc.perform(get("/api/transport?name=Standard Kart"))
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

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Transport transport = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                when(transportRepository.findById(eq("Standard Kart"))).thenReturn(Optional.of(transport));

                // act
                MvcResult response = mockMvc.perform(get("/api/transport?name=Standard Kart"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(transportRepository, times(1)).findById(eq("Standard Kart"));
                String expectedJson = mapper.writeValueAsString(transport);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(transportRepository.findById(eq("Standard Bike"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/transport?name=Standard Bike"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(transportRepository, times(1)).findById(eq("Standard Bike"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Transport with id Standard Bike not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_transport() throws Exception {

                // arrange

                Transport kart = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                Transport bike = Transport.builder()
                                .name("Inkstriker")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                ArrayList<Transport> expectedTransport = new ArrayList<>();
                expectedTransport.addAll(Arrays.asList(kart, bike));

                when(transportRepository.findAll()).thenReturn(expectedTransport);

                // act
                MvcResult response = mockMvc.perform(get("/api/transport/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(transportRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedTransport);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_transport() throws Exception {
                // arrange

                Transport scooter = Transport.builder()
                                .name("Lime")
                                .mode("scooter")
                                .cost("1.77")
                                .build();

                when(transportRepository.save(eq(scooter))).thenReturn(scooter);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/transport/post?name=Lime&mode=scooter&cost=1.77")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).save(scooter);
                String expectedJson = mapper.writeValueAsString(scooter);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Transport car = Transport.builder()
                                .name("Car")
                                .mode("car")
                                .cost("11000")
                                .build();

                when(transportRepository.findById(eq("Car"))).thenReturn(Optional.of(car));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/transport?name=Car")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById("Car");
                verify(transportRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id Car deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_transport_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(transportRepository.findById(eq("Standard Bike"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/transport?name=Standard Bike")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById("Standard Bike");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id Standard Bike not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_transport() throws Exception {
                // arrange

                Transport KartOrig = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1000")
                                .build();

                Transport KartEdited = Transport.builder()
                                .name("Standard Kart")
                                .mode("Kart")
                                .cost("1")
                                .build();

                String requestBody = mapper.writeValueAsString(KartEdited);

                when(transportRepository.findById(eq("Standard Kart"))).thenReturn(Optional.of(KartOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/transport?name=Standard Kart")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById("Standard Kart");
                verify(transportRepository, times(1)).save(KartEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_transport_that_does_not_exist() throws Exception {
                // arrange

                Transport editedBike = Transport.builder()
                                .name("Standard Bike")
                                .mode("Bike")
                                .cost("10101")
                                .build();

                String requestBody = mapper.writeValueAsString(editedBike);

                when(transportRepository.findById(eq("Standard Bike"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/transport?name=Standard Bike")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(transportRepository, times(1)).findById("Standard Bike");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Transport with id Standard Bike not found", json.get("message"));

        }
}
