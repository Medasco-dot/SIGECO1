package com.carfo.contentieux.config;

import com.carfo.contentieux.exception.DuplicateResourceException;
import com.carfo.contentieux.exception.InvalidFileException;
import com.carfo.contentieux.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation échouée");
        problemDetail.setTitle("Erreur de validation");
        problemDetail.setType(URI.create("urn:error:validation"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        problemDetail.setProperty("errors", errors);
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Ressource introuvable");
        problemDetail.setType(URI.create("urn:error:not-found"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFile(InvalidFileException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Fichier invalide");
        problemDetail.setType(URI.create("urn:error:invalid-file"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Conflit de ressource");
        problemDetail.setType(URI.create("urn:error:conflict"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        log.error("Failed to read request body", ex);
        Throwable cause = ex.getCause();
        String detail;
        if (cause instanceof UnrecognizedPropertyException unrecognized) {
            String property = unrecognized.getPropertyName();
            detail = "Le champ '" + property + "' n'est pas autorisé dans ce payload. Il est généré par le serveur ou ne fait pas partie de l'API.";
        } else if (cause instanceof InvalidFormatException invalidFormat) {
            detail = "Le champ '" + invalidFormat.getPath().get(0).getFieldName() + "' a un format invalide : " + invalidFormat.getOriginalMessage();
        } else {
            detail = "Impossible de lire la requête : " + (cause != null ? cause.getMessage() : ex.getMessage());
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Requête invalide - lecture du corps");
        problemDetail.setType(URI.create("urn:error:bad-request"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("exception", ex.getClass().getSimpleName());
        problemDetail.setProperty("message", ex.getMessage());
        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Requête invalide");
        problemDetail.setType(URI.create("urn:error:bad-request"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue. Veuillez réessayer ultérieurement.");
        problemDetail.setTitle("Erreur serveur");
        problemDetail.setType(URI.create("urn:error:server-error"));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }
}
