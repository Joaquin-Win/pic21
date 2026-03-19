package com.pic21.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una reunión educativa en PIC21.
 */
@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.NO_INICIADA;

    /**
     * Código de acceso opcional. Puede usarse para validar la entrada
     * de estudiantes a la reunión en fases futuras.
     */
    @Column(name = "access_code", length = 30)
    private String accessCode;

    /**
     * Data del archivo PDF adjunto a la reunión (opcional).
     * EAGER porque si fuera LAZY, Hibernate 6 lanzaría LazyInitializationException
     * cuando mapToResponse accede al campo fuera del ciclo de flush.
     */
    @Lob
    @Basic(fetch = jakarta.persistence.FetchType.EAGER)
    @Column(name = "pdf_file_data")
    private byte[] pdfFileData;

    /**
     * Nombre del archivo PDF adjunto.
     */
    @Column(name = "pdf_file_name")
    private String pdfFileName;

    /**
     * Usuario que creó la reunión.
     * EAGER porque lazy loading fuera de contexto de sesión Hibernate (en findById)
     * causaría LazyInitializationException.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
