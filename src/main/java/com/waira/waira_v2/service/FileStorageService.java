package com.waira.waira_v2.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootLocation;
    private final Path serviciosLocation;

    public FileStorageService(@Value("${app.uploads.root:uploads}") String uploadsRoot) {
        this.rootLocation = Paths.get(uploadsRoot).toAbsolutePath().normalize();
        this.serviciosLocation = this.rootLocation.resolve("servicios");
        init();
    }

    private void init() {
        try {
            Files.createDirectories(serviciosLocation);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el directorio de uploads", e);
        }
    }

    public String saveServicioImage(MultipartFile file) {
        @SuppressWarnings("null")
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagen");
        String extension = "";
        int dotIndex = original.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = original.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;
        Path destination = serviciosLocation.resolve(filename);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/servicios/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("Error guardando imagen", e);
        }
    }

    public void deleteServicioImage(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        String sanitized = url.trim().replace('\\', '/');
        if (sanitized.startsWith("http")) {
            return;
        }
        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        if (sanitized.startsWith("uploads/")) {
            sanitized = sanitized.substring("uploads/".length());
        }
        if (!StringUtils.hasText(sanitized)) {
            return;
        }
        Path target = rootLocation.resolve(sanitized).normalize();
        if (!target.startsWith(rootLocation)) {
            logger.warn("Ruta de imagen fuera del directorio permitido: {}", url);
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            logger.warn("No se pudo eliminar la imagen {}", url, e);
        }
    }
}
