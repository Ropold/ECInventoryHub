package ropold.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            when(cloudinary.uploader()).thenReturn(uploader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void uploadImage_ValidImage_ReturnsSecureUrl() throws IOException {
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.getOriginalFilename()).thenReturn("image.jpg");
        doAnswer(invocation -> {
            File file = invocation.getArgument(0);
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            return null;
        }).when(mockImage).transferTo(any(File.class));

        Map<String, String> mockUploadResult = Map.of("secure_url", "https://example.com/image.jpg");
        when(uploader.upload(any(File.class), eq(Collections.emptyMap()))).thenReturn(mockUploadResult);

        assertEquals("https://example.com/image.jpg", cloudinaryService.uploadImage(mockImage));
        verify(uploader, times(1)).upload(any(File.class), eq(Collections.emptyMap()));
    }

    @Test
    void uploadImage_ThrowsIOException_ThrowsException() throws IOException {
        MultipartFile mockImage = mock(MultipartFile.class);
        when(mockImage.getOriginalFilename()).thenReturn("image.jpg");
        doThrow(IOException.class).when(mockImage).transferTo(any(File.class));

        assertThrows(IOException.class, () -> cloudinaryService.uploadImage(mockImage));
        verify(uploader, never()).upload(any(File.class), eq(Collections.emptyMap()));
    }

    @Test
    void uploadFile_ValidFile_ReturnsSecureUrl() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        doAnswer(invocation -> {
            File file = invocation.getArgument(0);
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            return null;
        }).when(mockFile).transferTo(any(File.class));

        Map<String, String> mockUploadResult = Map.of("secure_url", "https://example.com/document.pdf");
        when(uploader.upload(any(File.class), eq(Map.of("resource_type", "raw")))).thenReturn(mockUploadResult);

        assertEquals("https://example.com/document.pdf", cloudinaryService.uploadFile(mockFile));
        verify(uploader, times(1)).upload(any(File.class), eq(Map.of("resource_type", "raw")));
    }

    @Test
    void uploadFile_ThrowsIOException_ThrowsException() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        doThrow(IOException.class).when(mockFile).transferTo(any(File.class));

        assertThrows(IOException.class, () -> cloudinaryService.uploadFile(mockFile));
        verify(uploader, never()).upload(any(File.class), eq(Map.of("resource_type", "raw")));
    }

    @Test
    void deleteImage_ValidImageUrl_DeletesImage() throws IOException {
        String imageUrl = "https://example.com/image.jpg";
        String publicId = "image";
        when(cloudinary.uploader().destroy(publicId, Collections.emptyMap())).thenReturn(Map.of("result", "ok"));

        cloudinaryService.deleteImage(imageUrl);

        verify(cloudinary.uploader(), times(1)).destroy(publicId, Collections.emptyMap());
    }

    @Test
    void deleteImage_ThrowsIOException_ThrowsRuntimeException() throws IOException {
        String imageUrl = "https://example.com/image.jpg";
        String publicId = "image";
        when(cloudinary.uploader().destroy(publicId, Collections.emptyMap())).thenThrow(new IOException("Delete failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cloudinaryService.deleteImage(imageUrl));
        assertEquals("Error deleting image from Cloudinary: " + publicId, exception.getMessage());
    }

    @Test
    void deleteFile_ValidFileUrl_DeletesFile() throws IOException {
        String fileUrl = "https://example.com/document.pdf";
        String publicId = "document";
        when(cloudinary.uploader().destroy(publicId, Map.of("resource_type", "raw"))).thenReturn(Map.of("result", "ok"));

        cloudinaryService.deleteFile(fileUrl);

        verify(cloudinary.uploader(), times(1)).destroy(publicId, Map.of("resource_type", "raw"));
    }

    @Test
    void deleteFile_ThrowsIOException_ThrowsRuntimeException() throws IOException {
        String fileUrl = "https://example.com/document.pdf";
        String publicId = "document";
        when(cloudinary.uploader().destroy(publicId, Map.of("resource_type", "raw"))).thenThrow(new IOException("Delete failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cloudinaryService.deleteFile(fileUrl));
        assertEquals("Error deleting file from Cloudinary: " + publicId, exception.getMessage());
    }
}
