package com.chatop.api.rental.dto;

import com.chatop.api.rental.entity.RentalEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Location exposée par l'API")
public record RentalResponse(
        @Schema(description = "Identifiant de la location", example = "1")
        Long id,
        @Schema(description = "Nom", example = "Maison du lac")
        String name,
        @Schema(description = "Surface en mètres carrés", example = "85.50")
        BigDecimal surface,
        @Schema(description = "Prix", example = "120.00")
        BigDecimal price,
        @Schema(description = "URL publique de l'image", example = "http://localhost:3001/uploads/image.png")
        String picture,
        @Schema(description = "Description", example = "Maison calme proche du lac")
        String description,
        @Schema(description = "Identifiant du propriétaire", example = "1")
        Long ownerId,
        @Schema(description = "Date de création", example = "2026-08-02T10:00:00Z")
        Instant createdAt,
        @Schema(description = "Date de dernière modification", example = "2026-08-02T10:00:00Z")
        Instant updatedAt
) {
    public static RentalResponse from(RentalEntity rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getName(),
                rental.getSurface(),
                rental.getPrice(),
                rental.getPicture(),
                rental.getDescription(),
                rental.getOwner().getId(),
                rental.getCreatedAt(),
                rental.getUpdatedAt()
        );
    }
}
