package com.powerstock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerstock.dto.request.LoginRequest;
import com.powerstock.dto.request.RegisterRequest;
import com.powerstock.dto.request.UpdatePermissionRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LocationRepository locationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;

    private String adminToken;
    private String staffToken;
    private Long staffUserId;

    private String registerUser(String username, String email, Long locationId) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword("password123");
        req.setLocationId(locationId);
        String resp = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("data").get("accessToken").asText();
    }

    private String loginAs(String username) throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword("password123");
        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("data").get("accessToken").asText();
    }

    @BeforeEach
    void setUp() throws Exception {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        locationRepository.deleteAll();

        Location location = Location.builder().name("WH").active(true).build();
        location = locationRepository.save(location);

        registerUser("admin", "admin@test.com", location.getId());
        registerUser("staff", "staff@test.com", location.getId());

        // Grant admin USER_READ and PERMISSION_MANAGE via repository
        User admin = userRepository.findByUsername("admin").orElseThrow();
        admin.setPermissionMask(admin.getPermissionMask()
                | Permission.USER_READ.getMask()
                | Permission.PERMISSION_MANAGE.getMask());
        userRepository.save(admin);

        adminToken = loginAs("admin");

        User staff = userRepository.findByUsername("staff").orElseThrow();
        staffUserId = staff.getId();
        staffToken = loginAs("staff");
    }

    @Test
    void shouldDenyItemCreateWithoutPermission() throws Exception {
        String itemJson = "{\"sku\":\"NO-PERM\",\"name\":\"No Permission\",\"basePrice\":10.00}";
        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowItemCreateAfterPermissionGrant() throws Exception {
        long mask = com.powerstock.model.enums.Permission.ITEM_READ.getMask()
                | com.powerstock.model.enums.Permission.ITEM_CREATE.getMask()
                | com.powerstock.model.enums.Permission.TRANSACTION_READ.getMask()
                | com.powerstock.model.enums.Permission.LOCATION_READ.getMask()
                | com.powerstock.model.enums.Permission.UNIT_READ.getMask();
        UpdatePermissionRequest permReq = new UpdatePermissionRequest();
        permReq.setPermissionMask(mask);

        mockMvc.perform(put("/api/users/" + staffUserId + "/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permReq)))
                .andExpect(status().isOk());

        String newToken = loginAs("staff");
        String itemJson = "{\"sku\":\"PERM-TEST\",\"name\":\"Perm Test\",\"basePrice\":10.00}";
        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + newToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("PERM-TEST"));
    }

    @Test
    void shouldDenyUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUserDeleteWithoutPermission() throws Exception {
        mockMvc.perform(delete("/api/users/" + staffUserId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowReportsWithPermission() throws Exception {
        long mask = com.powerstock.model.enums.Permission.ITEM_READ.getMask()
                | com.powerstock.model.enums.Permission.TRANSACTION_READ.getMask()
                | com.powerstock.model.enums.Permission.LOCATION_READ.getMask()
                | com.powerstock.model.enums.Permission.UNIT_READ.getMask()
                | com.powerstock.model.enums.Permission.REPORT_READ.getMask()
                | com.powerstock.model.enums.Permission.USER_READ.getMask()
                | com.powerstock.model.enums.Permission.USER_UPDATE.getMask()
                | com.powerstock.model.enums.Permission.USER_DELETE.getMask()
                | com.powerstock.model.enums.Permission.PERMISSION_MANAGE.getMask();
        UpdatePermissionRequest permReq = new UpdatePermissionRequest();
        permReq.setPermissionMask(mask);

        mockMvc.perform(put("/api/users/" + staffUserId + "/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permReq)))
                .andExpect(status().isOk());

        String newToken = loginAs("staff");
        mockMvc.perform(get("/api/reports/inventory-value")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }
}
