package com.pic21.repository;

import com.pic21.domain.MeetingFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingFileRepository extends JpaRepository<MeetingFile, Long> {

    List<MeetingFile> findByMeetingIdOrderByUploadedAtDesc(Long meetingId);

    /** Lista todos los archivos del sistema con datos básicos (sin fileData para no cargar blobs) */
    @Query("SELECT f FROM MeetingFile f JOIN FETCH f.meeting m JOIN FETCH f.uploadedBy u ORDER BY f.uploadedAt DESC")
    List<MeetingFile> findAllWithDetails();

    void deleteByMeetingId(Long meetingId);
}
