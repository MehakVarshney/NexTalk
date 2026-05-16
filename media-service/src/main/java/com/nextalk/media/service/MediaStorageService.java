package com.nextalk.media.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.nextalk.media.dto.MediaResponse;
import com.nextalk.media.entity.MediaFile;
import com.nextalk.media.exception.ApiException;
import com.nextalk.media.repository.MediaFileRepository;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain"
    );

    private final MediaFileRepository mediaFileRepository;
    private final MediaMapper mapper;
    private final Path storagePath;
    private final int thumbnailWidth;
    private final int thumbnailHeight;

    public MediaStorageService(
            MediaFileRepository mediaFileRepository,
            MediaMapper mapper,
            @Value("${app.media.storage-path}") String storagePath,
            @Value("${app.media.thumbnail-width}") int thumbnailWidth,
            @Value("${app.media.thumbnail-height}") int thumbnailHeight
    ) {
        this.mediaFileRepository = mediaFileRepository;
        this.mapper = mapper;
        this.storagePath = Path.of(storagePath).toAbsolutePath().normalize();
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }

    @Transactional
    public MediaResponse upload(UUID ownerId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported file type: " + contentType);
        }

        try {
            Files.createDirectories(storagePath);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String extension = getExtension(originalName);
            String storedName = UUID.randomUUID() + extension;
            Path target = storagePath.resolve(storedName).normalize();
            file.transferTo(target);

            boolean image = contentType.startsWith("image/");
            String thumbnailPath = image ? createThumbnail(target, storedName) : null;

            MediaFile mediaFile = new MediaFile();
            mediaFile.setOwnerId(ownerId);
            mediaFile.setOriginalFileName(originalName);
            mediaFile.setStoredFileName(storedName);
            mediaFile.setFilePath(target.toString());
            mediaFile.setThumbnailPath(thumbnailPath);
            mediaFile.setContentType(contentType);
            mediaFile.setSizeBytes(file.getSize());
            mediaFile.setImage(image);

            return mapper.toResponse(mediaFileRepository.save(mediaFile));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }
    }

    @Transactional(readOnly = true)
    public Page<MediaResponse> getMyGallery(UUID ownerId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest request = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return mediaFileRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, request).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MediaResponse getMetadata(UUID currentUserId, UUID mediaId) {
        MediaFile mediaFile = findOwnedMedia(currentUserId, mediaId);
        return mapper.toResponse(mediaFile);
    }

    @Transactional(readOnly = true)
    public Resource loadFile(UUID currentUserId, UUID mediaId) {
        MediaFile mediaFile = findOwnedMedia(currentUserId, mediaId);
        return loadResource(mediaFile.getFilePath());
    }

    @Transactional(readOnly = true)
    public Resource loadPublicFile(UUID mediaId) {
        MediaFile mediaFile = getPublicMedia(mediaId);
        return loadResource(mediaFile.getFilePath());
    }

    @Transactional(readOnly = true)
    public Resource loadThumbnail(UUID currentUserId, UUID mediaId) {
        MediaFile mediaFile = findOwnedMedia(currentUserId, mediaId);
        if (mediaFile.getThumbnailPath() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Thumbnail not found");
        }
        return loadResource(mediaFile.getThumbnailPath());
    }

    @Transactional(readOnly = true)
    public Resource loadPublicThumbnail(UUID mediaId) {
        MediaFile mediaFile = getPublicMedia(mediaId);
        if (mediaFile.getThumbnailPath() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Thumbnail not found");
        }
        return loadResource(mediaFile.getThumbnailPath());
    }

    @Transactional(readOnly = true)
    public MediaFile getOwnedMedia(UUID currentUserId, UUID mediaId) {
        return findOwnedMedia(currentUserId, mediaId);
    }

    @Transactional(readOnly = true)
    public MediaFile getPublicMedia(UUID mediaId) {
        return mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media not found"));
    }

    @Transactional
    public void delete(UUID currentUserId, UUID mediaId) {
        MediaFile mediaFile = findOwnedMedia(currentUserId, mediaId);
        deleteQuietly(mediaFile.getFilePath());
        deleteQuietly(mediaFile.getThumbnailPath());
        mediaFileRepository.delete(mediaFile);
    }

    private String createThumbnail(Path source, String storedName) throws IOException {
        Path thumbnailsDir = storagePath.resolve("thumbnails");
        Files.createDirectories(thumbnailsDir);

        String thumbnailName = "thumb-" + storedName + ".jpg";
        Path thumbnailPath = thumbnailsDir.resolve(thumbnailName).normalize();

        Thumbnails.of(source.toFile())
                .size(thumbnailWidth, thumbnailHeight)
                .outputFormat("jpg")
                .toFile(thumbnailPath.toFile());

        return thumbnailPath.toString();
    }

    private Resource loadResource(String filePath) {
        try {
            Resource resource = new UrlResource(Path.of(filePath).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ApiException(HttpStatus.NOT_FOUND, "File not found");
            }
            return resource;
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "File not found");
        }
    }

    private MediaFile findOwnedMedia(UUID ownerId, UUID mediaId) {
        MediaFile mediaFile = mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media not found"));
        if (!mediaFile.getOwnerId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this media");
        }
        return mediaFile;
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private void deleteQuietly(String filePath) {
        if (filePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException exception) {
            // Database deletion should still continue if the local file is already gone.
        }
    }
}
