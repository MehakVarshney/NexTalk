package com.nextalk.media.service;

import org.springframework.stereotype.Component;

import com.nextalk.media.dto.MediaResponse;
import com.nextalk.media.entity.MediaFile;

@Component
public class MediaMapper {

    public MediaResponse toResponse(MediaFile mediaFile) {
        String fileUrl = "/api/media/files/" + mediaFile.getId();
        String thumbnailUrl = mediaFile.getThumbnailPath() == null
                ? null
                : "/api/media/files/" + mediaFile.getId() + "/thumbnail";

        return new MediaResponse(
                mediaFile.getId(),
                mediaFile.getOwnerId(),
                mediaFile.getOriginalFileName(),
                mediaFile.getContentType(),
                mediaFile.getSizeBytes(),
                mediaFile.isImage(),
                fileUrl,
                thumbnailUrl,
                mediaFile.getCreatedAt()
        );
    }
}
