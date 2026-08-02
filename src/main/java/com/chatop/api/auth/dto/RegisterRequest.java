package com.chatop.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Informations nécessaires à la création d'un compte")
public record RegisterRequest(
        @Schema(description = "Nom affiché", example = "Alice Martin")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
        String name,

        @Schema(description = "Adresse e-mail unique", example = "alice@example.com")
        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail est invalide")
        @Size(max = 255, message = "L'adresse e-mail ne peut pas dépasser 255 caractères")
        String email,

        @Schema(description = "Mot de passe, entre 3 et 72 caractères", example = "Secret123",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 3, max = 72, message = "Le mot de passe doit contenir entre 3 et 72 caractères")
        String password
) {
}
