package com.chatop.api.message.controller;

import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.service.MessageService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Messages", description = "Messages envoyés au sujet des locations")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @Operation(
            operationId = "createMessage",
            summary = "Envoyer un message",
            description = "Enregistre un message pour une location. L'auteur est toujours déduit du JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message envoyé",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corps de requête invalide",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Location introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MessageResponse> create(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        messageService.create(request, jwt.getSubject());
        return ResponseEntity.ok(new MessageResponse("Message send with success"));
    }
}
