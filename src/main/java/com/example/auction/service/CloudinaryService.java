package com.example.auction.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    
    private final Cloudinary cloudinary;
    
    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dsm7gxh0w",
            "api_key", "621793885574939",
            "api_secret", "2yW7IZx2uQqPOKp5LzM0b3AKdlk"
        ));
    }
    
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        // Check file size - 50MB limit for videos, 10MB for images
        long maxSize = file.getContentType() != null && file.getContentType().startsWith("video/") 
            ? 50 * 1024 * 1024  // 50MB for videos
            : 10 * 1024 * 1024; // 10MB for images
            
        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds limit. Max: " + (maxSize / 1024 / 1024) + "MB");
        }
        
        System.out.println("Uploading file: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes (" + (file.getSize() / 1024 / 1024) + " MB)");
        System.out.println("Content type: " + file.getContentType());
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "folder", "hutech-auction",
                "resource_type", "auto",
                "chunk_size", 6000000 // 6MB chunks for large files
            ));
        
        String url = uploadResult.get("secure_url").toString();
        System.out.println("Upload successful: " + url);
        
        return url;
    }
}
