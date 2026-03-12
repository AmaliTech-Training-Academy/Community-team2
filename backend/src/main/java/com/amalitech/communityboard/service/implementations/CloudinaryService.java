package com.amalitech.communityboard.service.implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Async("executor")
    public CompletableFuture<String> uploadImage(MultipartFile file) {
        validateImage(file);
        try {

            Map params = ObjectUtils.asMap(
                    "folder", "community-board/posts",
                    "public_id", UUID.randomUUID().toString()
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            return CompletableFuture.completedFuture(
                    uploadResult.get("secure_url").toString()
            );

        } catch (IOException e) {
            log.info("Image upload failed: {}", e.getMessage());
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void deleteImage(String publicId) {

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    private void validateImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        if (file.getSize() > 5_000_000) {
            throw new IllegalArgumentException("File size exceeds limit");
        }
    }
}
