package com.chatop.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Jeton d'authentification renvoyé après inscription ou connexion")
public record AuthResponse(
        @Schema(description = "JWT à envoyer avec le préfixe Bearer", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {
}
