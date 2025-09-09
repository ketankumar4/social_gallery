package com.example.gallery.dto;

public class PhotoDto {
    private Long id;
    private String fileName;
    private String fileUrl;

    public PhotoDto(Long id, String fileName, String fileUrl) {
        this.id = id;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
