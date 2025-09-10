package com.example.gallery.controller;

import com.example.gallery.dto.ApiResponse;
import com.example.gallery.dto.PhotoDto;
import com.example.gallery.dto.UserDto;
import com.example.gallery.model.User;
import com.example.gallery.repository.UserRepository;
import com.example.gallery.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/photos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> viewPhotos(Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();

        UserDto userDto = new UserDto(user.getId(), user.getUsername());

        List<PhotoDto> photos = photoService.getVisiblePhotos(user)
                .stream()
                .map(photo -> new PhotoDto(
                        photo.getId(),
                        photo.getFilename(),
                        "/uploads/" + photo.getFilename()))
                .toList();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", userDto);
        responseData.put("photos", photos);

        return ResponseEntity.ok(new ApiResponse<>("Photos fetched successfully", responseData));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> uploadPhoto(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        User user = userRepo.findByUsername(principal.getName()).get();
        photoService.uploadPhoto(file, user);
        return ResponseEntity.ok(new ApiResponse<>("Photo uploaded successfully", null));
    }

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(@PathVariable Long id, Principal principal) throws IOException {
        User user = userRepo.findByUsername(principal.getName()).get();
        photoService.deletePhoto(id, user);
        return ResponseEntity.ok(new ApiResponse<>("Photo deleted successfully", null));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepo.save(user);

        UserDto userDto = new UserDto(user.getId(), user.getUsername());
        return ResponseEntity.ok(new ApiResponse<>("User registered successfully", userDto));
    }

    @PutMapping("/reset-password/{username}")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable String username, @RequestParam String newPassword) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepo.save(user);

        return ResponseEntity.ok(new ApiResponse<>("Password reset successfully", null));
    }

    @GetMapping("/photos/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> viewUserPhotos(@PathVariable Long userId, Principal principal) {
        User currentUser = userRepo.findByUsername(principal.getName()).orElseThrow();
        User targetUser = userRepo.findById(userId).orElseThrow();

        List<PhotoDto> photos = photoService.getUserVisiblePhotos(currentUser, targetUser)
                .stream()
                .map(photo -> new PhotoDto(
                        photo.getId(),
                        photo.getFilename(),
                        "/uploads/" + photo.getFilename()))
                .toList();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", new UserDto(targetUser.getId(), targetUser.getUsername()));
        responseData.put("photos", photos);

        return ResponseEntity.ok(new ApiResponse<>(
                "Photos fetched successfully",
                responseData
        ));
    }
}

