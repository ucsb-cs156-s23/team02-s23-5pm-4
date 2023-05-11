package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Tree;
import edu.ucsb.cs156.example.repositories.TreeRepository;

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

@WebMvcTest(controllers = TreeController.class)
@Import(TestConfig.class)
public class TreeControllerTests extends ControllerTestCase {

        @MockBean
        TreeRepository treeRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/tree/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/tree/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/tree/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/tree?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/tree/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/tree/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/tree/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Tree tree = Tree.builder()
                                .name("Birch")
                                .category("Decidous")
                                .build();

                when(treeRepository.findById(eq(7L))).thenReturn(Optional.of(tree));

                // act
                MvcResult response = mockMvc.perform(get("/api/tree?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(treeRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(tree);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(treeRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/tree?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(treeRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Tree with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_trees() throws Exception {

                // arrange

                Tree tree1 = Tree.builder()
                                .name("Birch")
                                .category("Decidous")
                                .build();


                Tree tree2 = Tree.builder()
                                .name("Pine")
                                .category("Coniferous")
                                .build();

                ArrayList<Tree> expectedTrees = new ArrayList<>();
                expectedTrees.addAll(Arrays.asList(tree1, tree2));

                when(treeRepository.findAll()).thenReturn(expectedTrees);

                // act
                MvcResult response = mockMvc.perform(get("/api/tree/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(treeRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedTrees);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_tree() throws Exception {
                // arrange


                Tree tree1 = Tree.builder()
                            .name("Birch")
                            .category("Decidous")
                            .build();

                when(treeRepository.save(eq(tree1))).thenReturn(tree1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/tree/post?name=Birch&category=Decidous")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(treeRepository, times(1)).save(tree1);
                String expectedJson = mapper.writeValueAsString(tree1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange


                Tree tree1 = Tree.builder()
                            .name("Birch")
                            .category("Decidous")
                            .build();

                when(treeRepository.findById(eq(15L))).thenReturn(Optional.of(tree1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/tree?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(treeRepository, times(1)).findById(15L);
                verify(treeRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Tree with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_tree_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(treeRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/tree?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(treeRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Tree with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_tree() throws Exception {
                // arrange


                Tree treeOrig = Tree.builder()
                            .name("Birch")
                            .category("Decidous")
                            .build();

                Tree treeEdited = Tree.builder()
                                .name("Maple")
                                .category("Decidous")
                                .build();

                String requestBody = mapper.writeValueAsString(treeEdited);

                when(treeRepository.findById(eq(67L))).thenReturn(Optional.of(treeOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/tree?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(treeRepository, times(1)).findById(67L);
                verify(treeRepository, times(1)).save(treeEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_tree_that_does_not_exist() throws Exception {
                // arrange


                Tree editedTree = Tree.builder()
                                .name("Birch")
                                .category("Decidous")
                                .build();

                String requestBody = mapper.writeValueAsString(editedTree);

                when(treeRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/tree?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(treeRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Tree with id 67 not found", json.get("message"));

        }
}
