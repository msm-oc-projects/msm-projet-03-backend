package com.chatop.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Identifiants de connexion")
public record LoginRequest(
        @Schema(description = "Adresse e-mail du compte", example = "alice@example.com")
        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 255, message = "L'adresse e-mail ne peut pas dépasser 255 caractères")
        String email,

        @Schema(description = "Mot de passe du compte", example = "Secret123",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(max = 72, message = "Le mot de passe ne peut pas dépasser 72 caractères")
        String password
) {
}
