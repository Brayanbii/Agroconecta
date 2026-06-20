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

    private final MongoImageService mongoImageService;

    public UploadFileService(MongoImageService mongoImageService) {
        this.mongoImageService = mongoImageService;
    }

    public String saveImage(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return mongoImageService.saveImage(file);
        }
        return "default.jpg";
    }

    public void deleteImage(String nombre) {
        if (nombre == null || nombre.isEmpty() || "default.jpg".equals(nombre)) {
            return;
        }
        // Si es un ObjectId de MongoDB (24 caracteres hex), borrar de GridFS
        if (nombre.matches("^[a-fA-F0-9]{24}$")) {
            mongoImageService.deleteImage(nombre);
            return;
        }
        // Fallback: borrar de sistema de archivos local
        Path path = Paths.get(folder + nombre);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}