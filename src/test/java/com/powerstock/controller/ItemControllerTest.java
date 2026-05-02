package com.powerstock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerstock.dto.request.ItemRequest;
import com.powerstock.dto.request.LoginRequest;
import com.powerstock.dto.request.RegisterRequest;
import com.powerstock.model.entity.Location;
import com.powerstock.model.entity.User;
import com.powerstock.model.enums.Permission;
import com.powerstock.repository.ItemRepository;
import com.powerstock.repository.LocationRepository;
import com.powerstock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LocationRepository locationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        locationRepository.deleteAll();

        Location location = Location.builder().name("Test Warehouse").active(true).build();
        location = locationRepository.save(location);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setLocationId(location.getId());

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Grant ITEM_CREATE permission for tests that need to create items
        User user = userRepository.findByUsername("testuser").orElseThrow();
        user.setPermissionMask(user.getPermissionMask() | Permission.ITEM_CREATE.getMask());
        userRepository.save(user);

        // Re-login to get a token with the updated permissions
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        authToken = jsonNode.get("data").get("accessToken").asText();
    }

    @Test
    void shouldCreateAndRetrieveItem() throws Exception {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-001");
        request.setName("Test Item");
        request.setDescription("A test item");
        request.setCategory("Electronics");
        request.setBasePrice(BigDecimal.valueOf(99.99));
        request.setReorderLevel(5);
        request.setReorderQuantity(10);

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-001"))
                .andExpect(jsonPath("$.data.name").value("Test Item"))
                .andExpect(jsonPath("$.data.basePrice").value(99.99));
    }

    @Test
    void shouldRejectDuplicateSku() throws Exception {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-DUP");
        request.setName("First Item");
        request.setBasePrice(BigDecimal.TEN);

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchItems() throws Exception {
        ItemRequest request = new ItemRequest();
        request.setSku("SKU-SEARCH");
        request.setName("Searchable Item");
        request.setBasePrice(BigDecimal.TEN);

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/items?search=Searchable")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Searchable Item"));
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isForbidden());
    }
}
