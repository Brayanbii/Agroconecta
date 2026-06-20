package com.proyecto.AccesoUsuarios.controller;

import com.proyecto.AccesoUsuarios.service.MongoImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ImageController {

    @Autowired
    private MongoImageService mongoImageService;

    @GetMapping("/images/{fileId}")
    public ResponseEntity<InputStreamResource> serveImage(@PathVariable String fileId) {
        // Si es un ObjectId de MongoDB (24 caracteres hex), servir desde GridFS
        if (fileId.matches("^[a-fA-F0-9]{24}$")) {
            GridFsResource resource = mongoImageService.getImage(fileId);
            if (resource != null) {
                try {
                    InputStream inputStream = resource.getInputStream();
                    MediaType mediaType = MediaType.parseMediaType(
                            resource.getContentType() != null ? resource.getContentType() : "image/jpeg"
                    );
                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .body(new InputStreamResource(inputStream));
                } catch (IOException e) {
                    return ResponseEntity.notFound().build();
                }
            }
            return ResponseEntity.notFound().build();
        }

        if ("default.jpg".equals(fileId)) {
            fileId = "default.png";
        }

        // Fallback: servir desde sistema de archivos local (dev / migración)
        try {
            Path imagePath = Paths.get("images/" + fileId);
            if (Files.exists(imagePath)) {
                InputStream inputStream = Files.newInputStream(imagePath);
                String contentType = Files.probeContentType(imagePath);
                MediaType mediaType = contentType != null
                        ? MediaType.parseMediaType(contentType)
                        : MediaType.IMAGE_JPEG;
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(new InputStreamResource(inputStream));
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.notFound().build();
    }
}
