package com.example.gallery.service;

import com.example.gallery.config.UploadProperties;
import com.example.gallery.model.Photo;
import com.example.gallery.model.User;
import com.example.gallery.repository.PhotoRepository;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepo;
    private final UploadProperties uploadProperties;

    // @Autowired
    public PhotoService(PhotoRepository photoRepo, UploadProperties uploadProperties) {
        this.photoRepo = photoRepo;
        this.uploadProperties = uploadProperties;
    }

    public void uploadPhoto(MultipartFile file, User user) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadProperties.getDir(), filename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        Photo photo = new Photo();
        photo.setFilename(filename);
        photo.setUploadedBy(user);
        photo.setUploadTime(LocalDateTime.now());
        photoRepo.save(photo);
    }

    public void deletePhoto(Long id, User user) throws IOException {
        Photo photo = photoRepo.findById(id).orElseThrow();
        if (photo.getUploadedBy().getId().equals(user.getId())) {
            Files.deleteIfExists(Paths.get(uploadProperties.getDir(), photo.getFilename()));
            photoRepo.delete(photo);
        }
    }

    public List<Photo> getAllPhotos() {
        return photoRepo.findAll();
    }
}
