package com.chatop.api.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmation d'une opération")
public record MessageResponse(
        @Schema(description = "Message de confirmation", example = "Rental created !")
        String message
) {
}
