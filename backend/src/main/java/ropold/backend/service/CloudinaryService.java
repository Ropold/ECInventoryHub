package ropold.backend.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile image) throws IOException {
        File fileToUpload = File.createTempFile("ec-inventory-hub", "");
        image.transferTo(fileToUpload);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileToUpload, Collections.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        File fileToUpload = File.createTempFile("ec-inventory-hub", "");
        file.transferTo(fileToUpload);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileToUpload, Map.of("resource_type", "raw"));
        return uploadResult.get("secure_url").toString();
    }

    private String extractPublicIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1].split("\\.")[0];
    }

    public void deleteImage(String imageUrl) {
        String publicId = extractPublicIdFromUrl(imageUrl);

        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image from Cloudinary: " + publicId, e);
        }
    }

    public void deleteFile(String fileUrl) {
        String publicId = extractPublicIdFromUrl(fileUrl);

        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "raw"));
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file from Cloudinary: " + publicId, e);
        }
    }
}