package com.chatop.api.shared.error;

import com.chatop.api.auth.exception.EmailAlreadyUsedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        ApiError body = ApiError.withViolations(
                400, "Bad Request", "La requête contient des données invalides",
                request.getRequestURI(), violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(ApiError.of(
                400, "Bad Request", "Le corps JSON est absent ou invalide", request.getRequestURI()
        ));
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    ResponseEntity<ApiError> handleEmailAlreadyUsed(
            EmailAlreadyUsedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(ApiError.of(
                400, "Bad Request", exception.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(
                401, "Unauthorized", "Identifiants invalides", request.getRequestURI()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(
                404, "Not Found", exception.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ApiError> handleForbidden(
            ForbiddenOperationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(
                403, "Forbidden", exception.getMessage(), request.getRequestURI()
        ));
    }

    @ExceptionHandler({
            InvalidFileException.class,
            MaxUploadSizeExceededException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message;
        if (exception instanceof InvalidFileException) {
            message = exception.getMessage();
        } else if (exception instanceof MaxUploadSizeExceededException) {
            message = "L'image dépasse la taille maximale autorisée";
        } else {
            message = "Un paramètre de la requête est invalide";
        }
        return ResponseEntity.badRequest().body(ApiError.of(
                400, "Bad Request", message, request.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Erreur inattendue pendant le traitement de {}", request.getRequestURI(), exception);
        return ResponseEntity.internalServerError().body(ApiError.of(
                500, "Internal Server Error", "Une erreur interne est survenue", request.getRequestURI()
        ));
    }
}
