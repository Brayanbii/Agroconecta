package com.proyecto.AccesoUsuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadFileService {

    private final String folder = "images/";

    public String saveImage(MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            // Crear el directorio si no existe
            Path directorio = Paths.get(folder);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(folder + file.getOriginalFilename());
            Files.write(path, bytes);
            return file.getOriginalFilename();
        }
        return "default.jpg";
    }

    public void deleteImage(String nombre) {
        Path path = Paths.get(folder + nombre);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log del error pero no interrumpimos el flujo
            e.printStackTrace();
        }
    }
}