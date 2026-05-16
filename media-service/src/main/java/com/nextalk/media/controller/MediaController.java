package com.nextalk.media.controller;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import com.nextalk.media.dto.MediaResponse;
import com.nextalk.media.entity.MediaFile;
import com.nextalk.media.security.CurrentUser;
import com.nextalk.media.service.MediaStorageService;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse upload(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam("file") MultipartFile file
    ) {
        return mediaStorageService.upload(currentUser.getUserId(), file);
    }

    @GetMapping("/gallery")
    public Page<MediaResponse> getMyGallery(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return mediaStorageService.getMyGallery(currentUser.getUserId(), page, size);
    }

    @GetMapping("/{mediaId}")
    public MediaResponse getMetadata(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID mediaId
    ) {
        return mediaStorageService.getMetadata(currentUser.getUserId(), mediaId);
    }

    @GetMapping("/files/{mediaId}")
    public ResponseEntity<Resource> getFile(
            @PathVariable UUID mediaId
    ) {
        MediaFile mediaFile = mediaStorageService.getPublicMedia(mediaId);
        Resource resource = mediaStorageService.loadPublicFile(mediaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + mediaFile.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/files/{mediaId}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(
            @PathVariable UUID mediaId
    ) {
        Resource resource = mediaStorageService.loadPublicThumbnail(mediaId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @DeleteMapping("/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID mediaId
    ) {
        mediaStorageService.delete(currentUser.getUserId(), mediaId);
    }
}
