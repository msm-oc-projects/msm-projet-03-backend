package com.chatop.api.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Données multipart pour créer une location")
public class CreateRentalRequest {

    @Schema(description = "Nom de la location", example = "Maison du lac")
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    private String name;

    @Schema(description = "Surface en mètres carrés", example = "85.50")
    @NotNull(message = "La surface est obligatoire")
    @DecimalMin(value = "0.01", message = "La surface doit être strictement positive")
    @Digits(integer = 8, fraction = 2, message = "La surface est invalide")
    private BigDecimal surface;

    @Schema(description = "Prix de la location", example = "120.00")
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.00", message = "Le prix doit être positif")
    @Digits(integer = 10, fraction = 2, message = "Le prix est invalide")
    private BigDecimal price;

    @Schema(description = "Image JPEG, PNG, WebP ou GIF", type = "string", format = "binary")
    @NotNull(message = "L'image est obligatoire")
    private MultipartFile picture;

    @Schema(description = "Description de la location", example = "Maison calme proche du lac")
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSurface() {
        return surface;
    }

    public void setSurface(BigDecimal surface) {
        this.surface = surface;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
