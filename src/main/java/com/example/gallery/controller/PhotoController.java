package com.example.gallery.controller;

// import com.example.gallery.model.Photo;
import com.example.gallery.model.User;
import com.example.gallery.repository.UserRepository;
import com.example.gallery.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Controller
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/photos")
    public String viewPhotos(Model model, Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).get();
        model.addAttribute("user", user);
        model.addAttribute("photos", photoService.getAllPhotos());
        return "photos";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        User user = userRepo.findByUsername(principal.getName()).get();
        photoService.uploadPhoto(file, user);
        return "redirect:/photos";
    }

    @PostMapping("/delete/{id}")
    public String deletePhoto(@PathVariable Long id, Principal principal) throws IOException {
        User user = userRepo.findByUsername(principal.getName()).get();
        photoService.deletePhoto(id, user);
        return "redirect:/photos";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepo.save(user);
        return "redirect:/login";
    }
}

