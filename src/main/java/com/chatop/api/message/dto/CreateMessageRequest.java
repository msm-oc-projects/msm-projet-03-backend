package com.chatop.api.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Message envoyé au sujet d'une location")
public record CreateMessageRequest(
        @Schema(description = "Identifiant de la location", example = "1")
        @NotNull(message = "L'identifiant de la location est obligatoire")
        @Positive(message = "L'identifiant de la location doit être positif")
        Long rentalId,
        @Schema(description = "Identifiant utilisateur attendu par le front ; l'auteur réel vient du JWT",
                example = "2")
        @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
        @Positive(message = "L'identifiant de l'utilisateur doit être positif")
        Long userId,
        @Schema(description = "Contenu du message", example = "Cette maison est-elle disponible ?")
        @NotBlank(message = "Le message est obligatoire")
        @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères")
        String message
) {
}
