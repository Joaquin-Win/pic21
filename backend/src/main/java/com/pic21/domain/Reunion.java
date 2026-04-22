package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reuniones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoReunion estado = EstadoReunion.NO_INICIADA;

    @Column(name = "access_code", length = 1000)
    private String accessCode;

    @Column(name = "recording_link", length = 1000)
    private String recordingLink;

    /** Link de presentación (slides, canva, etc.) */
    @Column(name = "presentacion_link", length = 1000)
    private String presentacionLink;

    @Column(name = "news_link", length = 1000)
    private String newsLink;

    @Column(name = "activity_link", length = 1000)
    private String activityLink;

    /**
     * Links adicionales — almacenados como JSON array en TEXT.
     * Ejemplo: ["https://...", "https://..."]
     */
    @Column(name = "links_extra", columnDefinition = "TEXT")
    @Builder.Default
    private String linksExtraJson = "[]";

    /** Links adicionales de noticias (varias notas por reunión). */
    @Column(name = "news_links_extra_json", columnDefinition = "TEXT")
    @Builder.Default
    private String newsLinksExtraJson = "[]";

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "pdf_file_data")
    private byte[] pdfFileData;

    @Column(name = "pdf_file_name")
    private String pdfFileName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
