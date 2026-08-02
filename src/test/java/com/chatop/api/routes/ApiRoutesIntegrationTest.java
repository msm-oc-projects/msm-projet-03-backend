package com.chatop.api.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatop.api.message.entity.MessageEntity;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.entity.RentalEntity;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.entity.UserEntity;
import com.chatop.api.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiRoutesIntegrationTest {

    private static final byte[] PNG = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void cleanDatabase() {
        messageRepository.deleteAll();
        rentalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void readRoutesReturnFrontCompatibleDtosAndPublicPicture() throws Exception {
        String ownerToken = register("Alice", "alice@example.com");
        UserEntity owner = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        RentalEntity rental = createRental(ownerToken);

        mockMvc.perform(get("/api/rentals")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentals.length()").value(1))
                .andExpect(jsonPath("$.rentals[0].id").value(rental.getId()))
                .andExpect(jsonPath("$.rentals[0].name").value("Maison du lac"))
                .andExpect(jsonPath("$.rentals[0].owner_id").value(owner.getId()))
                .andExpect(jsonPath("$.rentals[0].picture").isString())
                .andExpect(jsonPath("$.rentals[0].created_at").isNotEmpty());

        mockMvc.perform(get("/api/rentals/{id}", rental.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Une maison paisible"))
                .andExpect(jsonPath("$.surface").value(85.5))
                .andExpect(jsonPath("$.price").value(120));

        mockMvc.perform(get("/api/user/{id}", owner.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        String uploadPath = rental.getPicture().substring(rental.getPicture().indexOf("/uploads/"));
        mockMvc.perform(get(uploadPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void onlyOwnerCanUpdateRentalAndPictureIsPreserved() throws Exception {
        String ownerToken = register("Alice", "owner@example.com");
        String otherToken = register("Bob", "other@example.com");
        RentalEntity rental = createRental(ownerToken);
        String originalPicture = rental.getPicture();

        mockMvc.perform(updateRental(rental.getId(), otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Seul le propriétaire peut modifier cette location"));

        mockMvc.perform(updateRental(rental.getId(), ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rental updated !"));

        RentalEntity updated = rentalRepository.findById(rental.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Maison rénovée");
        assertThat(updated.getPrice()).isEqualByComparingTo("145.50");
        assertThat(updated.getPicture()).isEqualTo(originalPicture);
    }

    @Test
    void messageAuthorIsTakenFromJwtRatherThanClientUserId() throws Exception {
        String ownerToken = register("Alice", "owner@example.com");
        String senderToken = register("Bob", "sender@example.com");
        UserEntity owner = userRepository.findByEmailIgnoreCase("owner@example.com").orElseThrow();
        UserEntity sender = userRepository.findByEmailIgnoreCase("sender@example.com").orElseThrow();
        RentalEntity rental = createRental(ownerToken);

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", bearer(senderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "rental_id", rental.getId(),
                                "user_id", owner.getId(),
                                "message", "Cette maison est-elle disponible ?"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message send with success"));

        MessageEntity saved = messageRepository.findAll().get(0);
        assertThat(saved.getUser().getId()).isEqualTo(sender.getId());
        assertThat(saved.getRental().getId()).isEqualTo(rental.getId());
    }

    @Test
    void invalidOrMissingResourcesReturnClientErrors() throws Exception {
        String token = register("Alice", "alice@example.com");

        mockMvc.perform(get("/api/rentals/999999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/user/999999")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/rentals/0")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        MockMultipartFile invalidPicture = new MockMultipartFile(
                "picture", "notes.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes()
        );
        mockMvc.perform(multipart("/api/rentals")
                        .file(invalidPicture)
                        .param("name", "Maison")
                        .param("surface", "50")
                        .param("price", "90")
                        .param("description", "Description")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le type de l'image n'est pas autorisé"));

        mockMvc.perform(post("/api/messages")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "rental_id", 999999,
                                "user_id", 1,
                                "message", "Bonjour"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void everyBusinessRouteRejectsMissingToken() throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "house.png", MediaType.IMAGE_PNG_VALUE, PNG
        );

        mockMvc.perform(get("/api/user/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/rentals")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/rentals/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(multipart("/api/rentals").file(picture)).andExpect(status().isUnauthorized());
        mockMvc.perform(multipart("/api/rentals/1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private RentalEntity createRental(String token) throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "house.png", MediaType.IMAGE_PNG_VALUE, PNG
        );
        mockMvc.perform(multipart("/api/rentals")
                        .file(picture)
                        .param("name", "Maison du lac")
                        .param("surface", "85.50")
                        .param("price", "120.00")
                        .param("description", "Une maison paisible")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rental created !"));
        return rentalRepository.findAll().get(0);
    }

    private MockHttpServletRequestBuilder updateRental(Long rentalId, String token) {
        return multipart("/api/rentals/{id}", rentalId)
                .param("name", "Maison rénovée")
                .param("surface", "90.00")
                .param("price", "145.50")
                .param("description", "Description mise à jour")
                .header("Authorization", bearer(token))
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
    }

    private String register(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "name", name,
                                "email", email,
                                "password", "Secret123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return body.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
