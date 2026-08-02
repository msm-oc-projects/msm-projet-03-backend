package com.chatop.api.user.dto;

import com.chatop.api.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Profil utilisateur sans mot de passe")
public record UserResponse(
        @Schema(description = "Identifiant", example = "1")
        Long id,
        @Schema(description = "Nom affiché", example = "Alice Martin")
        String name,
        @Schema(description = "Adresse e-mail", example = "alice@example.com")
        String email,
        @Schema(description = "Date de création", example = "2026-08-02T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Date de dernière modification", example = "2026-08-02T10:00:00Z")
        Instant updatedAt
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
