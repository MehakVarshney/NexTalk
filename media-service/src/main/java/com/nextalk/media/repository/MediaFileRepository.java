package com.nextalk.media.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nextalk.media.entity.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    Page<MediaFile> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);
}
