package com.chatop.api.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Erreur REST normalisée")
public record ApiError(
        @Schema(description = "Date de l'erreur", example = "2026-08-02T10:00:00Z")
        Instant timestamp,
        @Schema(description = "Statut HTTP", example = "400")
        int status,
        @Schema(description = "Libellé HTTP", example = "Bad Request")
        String error,
        @Schema(description = "Explication destinée au client", example = "La requête contient des données invalides")
        String message,
        @Schema(description = "Route appelée", example = "/api/rentals")
        String path,
        @Schema(description = "Erreurs de validation par champ")
        List<FieldViolation> violations
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError withViolations(
            int status,
            String error,
            String message,
            String path,
            List<FieldViolation> violations
    ) {
        return new ApiError(Instant.now(), status, error, message, path, List.copyOf(violations));
    }
}
