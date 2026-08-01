/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.ArchivoReunion
 *  com.pic21.domain.ArchivoReunion$ArchivoReunionBuilder
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  jakarta.persistence.Basic
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.JoinColumn
 *  jakarta.persistence.Lob
 *  jakarta.persistence.ManyToOne
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.CreationTimestamp
 */
package com.pic21.domain;
import lombok.Builder;

import com.pic21.domain.ArchivoReunion;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="meeting_files")
public class ArchivoReunion {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="file_name", nullable=false, length=255)
    private String fileName;
    @Column(name="file_type", nullable=false, length=100)
    private String fileType;
    @Lob
    @Basic(fetch=FetchType.LAZY)
    @Column(name="file_data", nullable=false)
    private byte[] fileData;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="meeting_id", nullable=false)
    private Reunion reunion;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="uploaded_by", nullable=false)
    private Usuario subidoPor;
    @CreationTimestamp
    @Column(name="uploaded_at", updatable=false)
    private LocalDateTime uploadedAt;
    public Long getId() {
        return this.id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public byte[] getFileData() {
        return this.fileData;
    }

    public Reunion getReunion() {
        return this.reunion;
    }

    public Usuario getSubidoPor() {
        return this.subidoPor;
    }

    public LocalDateTime getUploadedAt() {
        return this.uploadedAt;
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

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public void setSubidoPor(Usuario subidoPor) {
        this.subidoPor = subidoPor;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public ArchivoReunion() {
    }

    @Builder
    public ArchivoReunion(Long id, String fileName, String fileType, byte[] fileData, Reunion reunion, Usuario subidoPor, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.reunion = reunion;
        this.subidoPor = subidoPor;
        this.uploadedAt = uploadedAt;
    }
}

