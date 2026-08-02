package com.chatop.api.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Collection de locations")
public record RentalsResponse(
        @Schema(description = "Locations de la plus récente à la plus ancienne")
        List<RentalResponse> rentals
) {
    public RentalsResponse {
        rentals = List.copyOf(rentals);
    }
}
