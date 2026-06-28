package ropold.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private final CloudinaryService cloudinaryService;

    public String determineImageUrl(MultipartFile image, String frontendImageUrl, String existingImageUrl) throws IOException {
        if (image != null && !image.isEmpty()) {
            return cloudinaryService.uploadImage(image);
        } else if (frontendImageUrl == null || frontendImageUrl.isBlank()) {
            return null;
        } else {
            return existingImageUrl;
        }
    }

    public void cleanupOldImageIfNeeded(String oldImageUrl, String newImageUrl) {
        if (hasImage(oldImageUrl) && (!hasImage(newImageUrl) || !oldImageUrl.equals(newImageUrl))) {
            cloudinaryService.deleteImage(oldImageUrl);
        }
    }

    private boolean hasImage(String imageUrl) {
        return imageUrl != null && !imageUrl.isBlank();
    }
}
