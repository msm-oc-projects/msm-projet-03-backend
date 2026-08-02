package com.chatop.api.rental.controller;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.service.RentalService;
import com.chatop.api.shared.dto.MessageResponse;
import com.chatop.api.shared.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/rentals")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Locations", description = "Consultation et gestion des locations")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    @Operation(
            operationId = "getRentals",
            summary = "Lister les locations",
            description = "Renvoie toutes les locations, de la plus récente à la plus ancienne."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des locations",
                    content = @Content(schema = @Schema(implementation = RentalsResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RentalsResponse> getAll() {
        return ResponseEntity.ok(rentalService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getRentalById", summary = "Consulter une location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location trouvée",
                    content = @Content(schema = @Schema(implementation = RentalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identifiant invalide",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Location introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RentalResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(rentalService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "createRental",
            summary = "Créer une location",
            description = "Crée une location multipart avec une image. Le propriétaire est déduit du JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location créée",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données ou image invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> create(
            @Valid @ModelAttribute CreateRentalRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        rentalService.create(request, jwt.getSubject());
        return ResponseEntity.ok(new MessageResponse("Rental created !"));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "updateRental",
            summary = "Modifier une location",
            description = "Modifie une location existante sans remplacer son image. Réservé à son propriétaire."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location modifiée",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identifiant ou données invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Utilisateur non propriétaire",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Location introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> update(
            @PathVariable @Positive Long id,
            @Valid @ModelAttribute UpdateRentalRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        rentalService.update(id, request, jwt.getSubject());
        return ResponseEntity.ok(new MessageResponse("Rental updated !"));
    }
}
