package com.chatop.api.storage;

import com.chatop.api.shared.error.InvalidFileException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final StorageProperties properties;
    private final Path storageRoot;

    public FileStorageService(StorageProperties properties) {
        this.properties = properties;
        this.storageRoot = Path.of(properties.directory()).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initializeStorage() {
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'initialiser le dossier des images", exception);
        }
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("L'image est obligatoire");
        }
        if (file.getSize() > properties.maxFileSize().toBytes()) {
            throw new InvalidFileException("L'image dépasse la taille maximale autorisée");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED_IMAGE_TYPES.get(contentType);
        if (extension == null) {
            throw new InvalidFileException("Le type de l'image n'est pas autorisé");
        }

        String filename = UUID.randomUUID() + extension;
        Path destination = storageRoot.resolve(filename).normalize();
        if (!destination.getParent().equals(storageRoot)) {
            throw new InvalidFileException("Le nom du fichier est invalide");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de stocker l'image", exception);
        }

        return normalizedPublicBaseUrl() + "/uploads/" + filename;
    }

    private String normalizedPublicBaseUrl() {
        String baseUrl = properties.publicBaseUrl().trim();
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
