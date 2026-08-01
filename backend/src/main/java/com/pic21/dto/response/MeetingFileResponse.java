/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.MeetingFileResponse
 *  com.pic21.dto.response.MeetingFileResponse$MeetingFileResponseBuilder
 */
package com.pic21.dto.response;

import com.pic21.dto.response.MeetingFileResponse;
import java.time.LocalDateTime;

public class MeetingFileResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long meetingId;
    private String meetingTitle;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    private Long fileSize;

    MeetingFileResponse(Long id, String fileName, String fileType, Long meetingId, String meetingTitle, String uploadedByUsername, LocalDateTime uploadedAt, Long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.meetingId = meetingId;
        this.meetingTitle = meetingTitle;
        this.uploadedByUsername = uploadedByUsername;
        this.uploadedAt = uploadedAt;
        this.fileSize = fileSize;
    }

    public static MeetingFileResponseBuilder builder() {
        return new MeetingFileResponseBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public Long getMeetingId() {
        return this.meetingId;
    }

    public String getMeetingTitle() {
        return this.meetingTitle;
    }

    public String getUploadedByUsername() {
        return this.uploadedByUsername;
    }

    public LocalDateTime getUploadedAt() {
        return this.uploadedAt;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }

    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MeetingFileResponse)) {
            return false;
        }
        MeetingFileResponse other = (MeetingFileResponse)o;
        if (!other.canEqual((Object)this)) {
            return false;
        }
        Long this$id = this.getId();
        Long other$id = other.getId();
        if (this$id == null ? other$id != null : !((Object)this$id).equals(other$id)) {
            return false;
        }
        Long this$meetingId = this.getMeetingId();
        Long other$meetingId = other.getMeetingId();
        if (this$meetingId == null ? other$meetingId != null : !((Object)this$meetingId).equals(other$meetingId)) {
            return false;
        }
        Long this$fileSize = this.getFileSize();
        Long other$fileSize = other.getFileSize();
        if (this$fileSize == null ? other$fileSize != null : !((Object)this$fileSize).equals(other$fileSize)) {
            return false;
        }
        String this$fileName = this.getFileName();
        String other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) {
            return false;
        }
        String this$fileType = this.getFileType();
        String other$fileType = other.getFileType();
        if (this$fileType == null ? other$fileType != null : !this$fileType.equals(other$fileType)) {
            return false;
        }
        String this$meetingTitle = this.getMeetingTitle();
        String other$meetingTitle = other.getMeetingTitle();
        if (this$meetingTitle == null ? other$meetingTitle != null : !this$meetingTitle.equals(other$meetingTitle)) {
            return false;
        }
        String this$uploadedByUsername = this.getUploadedByUsername();
        String other$uploadedByUsername = other.getUploadedByUsername();
        if (this$uploadedByUsername == null ? other$uploadedByUsername != null : !this$uploadedByUsername.equals(other$uploadedByUsername)) {
            return false;
        }
        LocalDateTime this$uploadedAt = this.getUploadedAt();
        LocalDateTime other$uploadedAt = other.getUploadedAt();
        return !(this$uploadedAt == null ? other$uploadedAt != null : !((Object)this$uploadedAt).equals(other$uploadedAt));
    }

    protected boolean canEqual(Object other) {
        return other instanceof MeetingFileResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Long $id = this.getId();
        result = result * 59 + ($id == null ? 43 : ((Object)$id).hashCode());
        Long $meetingId = this.getMeetingId();
        result = result * 59 + ($meetingId == null ? 43 : ((Object)$meetingId).hashCode());
        Long $fileSize = this.getFileSize();
        result = result * 59 + ($fileSize == null ? 43 : ((Object)$fileSize).hashCode());
        String $fileName = this.getFileName();
        result = result * 59 + ($fileName == null ? 43 : $fileName.hashCode());
        String $fileType = this.getFileType();
        result = result * 59 + ($fileType == null ? 43 : $fileType.hashCode());
        String $meetingTitle = this.getMeetingTitle();
        result = result * 59 + ($meetingTitle == null ? 43 : $meetingTitle.hashCode());
        String $uploadedByUsername = this.getUploadedByUsername();
        result = result * 59 + ($uploadedByUsername == null ? 43 : $uploadedByUsername.hashCode());
        LocalDateTime $uploadedAt = this.getUploadedAt();
        result = result * 59 + ($uploadedAt == null ? 43 : ((Object)$uploadedAt).hashCode());
        return result;
    }

    public String toString() {
        return "MeetingFileResponse(id=" + this.getId() + ", fileName=" + this.getFileName() + ", fileType=" + this.getFileType() + ", meetingId=" + this.getMeetingId() + ", meetingTitle=" + this.getMeetingTitle() + ", uploadedByUsername=" + this.getUploadedByUsername() + ", uploadedAt=" + String.valueOf(this.getUploadedAt()) + ", fileSize=" + this.getFileSize() + ")";
    }
}

