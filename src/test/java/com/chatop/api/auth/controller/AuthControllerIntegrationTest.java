package com.chatop.api.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatop.api.user.entity.UserEntity;
import com.chatop.api.user.repository.UserRepository;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.repository.RentalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        rentalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerReturnsAWorkingTokenAndHashesPassword() throws Exception {
        String token = register("Marie", "Marie@Example.com", "Secret123");

        UserEntity savedUser = userRepository.findByEmailIgnoreCase("marie@example.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("Secret123");
        assertThat(passwordEncoder.matches("Secret123", savedUser.getPassword())).isTrue();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Marie"))
                .andExpect(jsonPath("$.email").value("marie@example.com"))
                .andExpect(jsonPath("$.created_at").isNotEmpty())
                .andExpect(jsonPath("$.updated_at").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginReturnsAWorkingToken() throws Exception {
        register("Marie", "marie@example.com", "Secret123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "email", "MARIE@example.com",
                                "password", "Secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = readToken(result);
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("marie@example.com"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        register("Marie", "marie@example.com", "Secret123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "name", "Autre Marie",
                                "email", "MARIE@example.com",
                                "password", "Different123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cette adresse e-mail est déjà utilisée"));
    }

    @Test
    void invalidPayloadIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.length()").value(3));
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        register("Marie", "marie@example.com", "Secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "email", "marie@example.com",
                                "password", "incorrect"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifiants invalides"));
    }

    @Test
    void protectedRouteRejectsMissingOrInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("ChâTop API"))
                .andExpect(jsonPath("$.paths['/api/auth/register'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/auth/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/user/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/rentals'].get").exists())
                .andExpect(jsonPath("$.paths['/api/rentals'].post").exists())
                .andExpect(jsonPath("$.paths['/api/rentals/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/rentals/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/messages'].post").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/rentals'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/rentals'].post.requestBody.content['multipart/form-data']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/rentals/{id}'].put.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/messages'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/user/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.components.schemas.ApiError").exists())
                .andExpect(jsonPath("$.components.schemas.RentalResponse").exists())
                .andExpect(jsonPath("$.components.schemas.UserResponse").exists());
    }

    private String register(String name, String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "name", name,
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return readToken(result);
    }

    private String readToken(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return body.get("token").asText();
    }
}
