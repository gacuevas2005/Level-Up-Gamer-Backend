package com.LevelUpGamer.proyecto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path rootLocation;

    // Inyecta la ruta que definimos en application.properties
    @Autowired
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        // Intenta crear el directorio si no existe
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio de subida", e);
        }
    }

    public String store(MultipartFile file, String newFilename) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Falló al guardar archivo vacío.");
        }

        // Resuelve la ruta completa del archivo
        Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename))
                .normalize().toAbsolutePath();

        // Copia el archivo al directorio de destino
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Devuelve el nombre del archivo guardado
        return newFilename;
    }
}