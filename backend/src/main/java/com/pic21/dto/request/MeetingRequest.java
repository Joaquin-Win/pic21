/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.request.MeetingRequest
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Size
 */
package com.pic21.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequest {
    @NotBlank(message="El t\u00edtulo es obligatorio")
    @Size(max=200)
    private @NotBlank(message="El t\u00edtulo es obligatorio") @Size(max=200) String title;
    @Size(max=2000)
    private @Size(max=2000) String description;
    @NotNull(message="La fecha y hora es obligatoria")
    private @NotNull(message="La fecha y hora es obligatoria") LocalDateTime scheduledAt;
    private String accessCode;
    private String recordingLink;
    private String presentacionLink;
    private String newsLink;
    private String activityLink;
    private List<String> linksExtra;
    private List<String> newsLinksExtra;

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getScheduledAt() {
        return this.scheduledAt;
    }

    public String getAccessCode() {
        return this.accessCode;
    }

    public String getRecordingLink() {
        return this.recordingLink;
    }

    public String getPresentacionLink() {
        return this.presentacionLink;
    }

    public String getNewsLink() {
        return this.newsLink;
    }

    public String getActivityLink() {
        return this.activityLink;
    }

    public List<String> getLinksExtra() {
        return this.linksExtra;
    }

    public List<String> getNewsLinksExtra() {
        return this.newsLinksExtra;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public void setRecordingLink(String recordingLink) {
        this.recordingLink = recordingLink;
    }

    public void setPresentacionLink(String presentacionLink) {
        this.presentacionLink = presentacionLink;
    }

    public void setNewsLink(String newsLink) {
        this.newsLink = newsLink;
    }

    public void setActivityLink(String activityLink) {
        this.activityLink = activityLink;
    }

    public void setLinksExtra(List<String> linksExtra) {
        this.linksExtra = linksExtra;
    }

    public void setNewsLinksExtra(List<String> newsLinksExtra) {
        this.newsLinksExtra = newsLinksExtra;
    }
}

