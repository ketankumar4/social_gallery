package com.example.gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.gallery.config.UploadProperties;
import com.example.gallery.config.JwtConfig;

@SpringBootApplication
@EnableConfigurationProperties({UploadProperties.class, JwtConfig.class})
public class GalleryApplication {
    public static void main(String[] args) {
        SpringApplication.run(GalleryApplication.class, args);
    }
}
