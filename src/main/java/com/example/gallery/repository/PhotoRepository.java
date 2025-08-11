package com.example.gallery.repository;

import com.example.gallery.model.Photo;
import com.example.gallery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByUploadedBy(User user);
}
