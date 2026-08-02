package com.chatop.api.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erreur de validation associée à un champ")
public record FieldViolation(
        @Schema(description = "Nom du champ", example = "email")
        String field,
        @Schema(description = "Contrainte non respectée", example = "L'adresse e-mail est invalide")
        String message
) {
}
