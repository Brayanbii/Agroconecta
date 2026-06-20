package com.proyecto.AccesoUsuarios.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MongoImageService {

    @Autowired
    private GridFsOperations gridFsOperations;

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "default.jpg";
        }
        ObjectId fileId = gridFsOperations.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        return fileId.toString();
    }

    public GridFsResource getImage(String fileId) {
        GridFSFile gridFSFile = gridFsOperations.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
        if (gridFSFile == null) {
            return null;
        }
        return gridFsOperations.getResource(gridFSFile);
    }

    public void deleteImage(String fileId) {
        gridFsOperations.delete(
                new Query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
    }
}
