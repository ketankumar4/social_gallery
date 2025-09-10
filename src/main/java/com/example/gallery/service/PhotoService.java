package com.example.gallery.service;

import com.example.gallery.config.UploadProperties;
import com.example.gallery.model.FriendRequest;
import com.example.gallery.model.Photo;
import com.example.gallery.model.User;
import com.example.gallery.repository.FriendRequestRepository;
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
    private final FriendRequestRepository friendRequestRepo;

    // @Autowired
    public PhotoService(PhotoRepository photoRepo, UploadProperties uploadProperties, FriendRequestRepository friendRequestRepo) {
        this.photoRepo = photoRepo;
        this.uploadProperties = uploadProperties;
        this.friendRequestRepo = friendRequestRepo;
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

    public List<Photo> getVisiblePhotos(User currentUser) {
        List<Photo> allPhotos = photoRepo.findAll();

        return allPhotos.stream()
                .filter(photo -> {
                    // always see your own photos
                    if (photo.getUploadedBy().equals(currentUser)) return true;

                    // check if friendship exists (in either direction)
                    boolean isFriend =
                        !friendRequestRepo.findBySenderAndReceiverAndStatus(
                                currentUser, photo.getUploadedBy(), FriendRequest.Status.ACCEPTED
                        ).isEmpty()
                        ||
                        !friendRequestRepo.findBySenderAndReceiverAndStatus(
                                photo.getUploadedBy(), currentUser, FriendRequest.Status.ACCEPTED
                        ).isEmpty();

                    return isFriend;
                })
                .toList();
    }

    public List<Photo> getUserVisiblePhotos(User currentUser, User targetUser) {
    // If same user → always visible
        if (currentUser.equals(targetUser)) {
            return photoRepo.findByUploadedBy(targetUser);
        }

        // Check friendship (either direction)
        boolean isFriend =
            !friendRequestRepo.findBySenderAndReceiverAndStatus(
                    currentUser, targetUser, FriendRequest.Status.ACCEPTED
            ).isEmpty()
            ||
            !friendRequestRepo.findBySenderAndReceiverAndStatus(
                    targetUser, currentUser, FriendRequest.Status.ACCEPTED
            ).isEmpty();

        if (!isFriend) {
            throw new RuntimeException("You are not allowed to view this user's photos");
        }

        return photoRepo.findByUploadedBy(targetUser);
    }
}
