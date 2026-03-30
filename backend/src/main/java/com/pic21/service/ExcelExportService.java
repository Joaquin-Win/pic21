package com.pic21.service;

import com.pic21.domain.Attendance;
import com.pic21.domain.Meeting;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AttendanceRepository;
import com.pic21.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio encargado de generar archivos Excel (.xlsx) usando Apache POI.
 *
 * Genera dos tipos de reportes:
 *   1. Asistencias de una reunión específica.
 *   2. Asistencias globales (todas las reuniones, todas las asistencias).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;

    // -----------------------------------------------------------------------
    // REPORTE 1: Asistencias de una reunión
    // -----------------------------------------------------------------------

    /**
     * Genera un archivo Excel con todas las asistencias de la reunión indicada.
     *
     * @param meetingId ID de la reunión
     * @return bytes del archivo .xlsx
     */
    @Transactional(readOnly = true)
    public byte[] exportAttendanceByMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));

        List<Attendance> attendances = attendanceRepository.findByMeetingWithDetails(meeting);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Asistencias");

            // ── Encabezado del reporte ──────────────────────────────────────
            CellStyle titleStyle = createTitleStyle(workbook);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Asistencias — " + meeting.getTitle());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Reunión:");
            infoRow.createCell(1).setCellValue(meeting.getTitle());
            Row dateRow = sheet.createRow(2);
            dateRow.createCell(0).setCellValue("Fecha:");
            dateRow.createCell(1).setCellValue(
                    meeting.getScheduledAt() != null ? meeting.getScheduledAt().format(DATE_FMT) : "-");
            Row countRow = sheet.createRow(3);
            countRow.createCell(0).setCellValue("Total asistentes:");
            countRow.createCell(1).setCellValue(attendances.size());

            // ── Cabecera de columnas ────────────────────────────────────────
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(5);
            String[] headers = {"#", "Usuario", "Nombre", "Apellido", "Correo", "Legajo", "Tipo de usuario", "Carrera", "Rol", "Registrado en"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Filas de datos ──────────────────────────────────────────────
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 6;
            int index = 1;
            for (Attendance a : attendances) {
                Row row = sheet.createRow(rowNum++);
                createDataCell(row, 0, String.valueOf(index++), dataStyle);
                createDataCell(row, 1, a.getUser().getUsername(), dataStyle);
                createDataCell(row, 2, nvl(a.getUser().getFirstName()), dataStyle);
                createDataCell(row, 3, nvl(a.getUser().getLastName()), dataStyle);
                createDataCell(row, 4, nvl(a.getUser().getEmail()), dataStyle);
                createDataCell(row, 5, nvl(a.getUser().getLegajo()), dataStyle);
                createDataCell(row, 6, nvl(a.getUser().getTipoUsuario()), dataStyle);
                createDataCell(row, 7, nvl(a.getUser().getCarrera()), dataStyle);
                createDataCell(row, 8, getUserRoles(a.getUser()), dataStyle);
                createDataCell(row, 9,
                        a.getRegisteredAt() != null ? a.getRegisteredAt().format(DATE_FMT) : "-", dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel generado: {} filas para reunión id={}", attendances.size(), meetingId);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }

    // -----------------------------------------------------------------------
    // REPORTE 2: Asistencias globales (todas las reuniones)
    // -----------------------------------------------------------------------

    /**
     * Genera un Excel con todas las asistencias del sistema, agrupadas por reunión.
     * Cada reunión ocupa una hoja (sheet) del libro.
     *
     * @return bytes del archivo .xlsx
     */
    @Transactional(readOnly = true)
    public byte[] exportAllAttendances() {
        List<Meeting> meetings = meetingRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ── Hoja resumen ────────────────────────────────────────────────
            Sheet summary = workbook.createSheet("Resumen");
            CellStyle titleStyle  = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle   = createDataStyle(workbook);

            Row summaryTitle = summary.createRow(0);
            Cell stc = summaryTitle.createCell(0);
            stc.setCellValue("Exportación Global de Asistencias — PIC21");
            stc.setCellStyle(titleStyle);
            summary.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            Row summaryHeader = summary.createRow(2);
            String[] sh = {"Reunión", "Fecha", "Estado", "Total asistentes"};
            for (int i = 0; i < sh.length; i++) {
                Cell c = summaryHeader.createCell(i);
                c.setCellValue(sh[i]);
                c.setCellStyle(headerStyle);
            }

            int summaryRowNum = 3;
            int totalGlobal = 0;
            int sheetIdx = 1;

            for (Meeting meeting : meetings) {
                List<Attendance> attendances = attendanceRepository.findByMeetingWithDetails(meeting);
                totalGlobal += attendances.size();

                // ── Fila en el resumen ──────────────────────────────────────
                Row sRow = summary.createRow(summaryRowNum++);
                createDataCell(sRow, 0, meeting.getTitle(), dataStyle);
                createDataCell(sRow, 1,
                        meeting.getScheduledAt() != null ? meeting.getScheduledAt().format(DATE_FMT) : "-",
                        dataStyle);
                createDataCell(sRow, 2, meeting.getStatus().name(), dataStyle);
                createDataCell(sRow, 3, String.valueOf(attendances.size()), dataStyle);

                // ── Hoja individual por reunión (nombre único garantizado) ──
                String sheetName = sanitizeSheetName(meeting.getTitle(), sheetIdx++);
                Sheet sheet = workbook.createSheet(sheetName);

                Row hRow = sheet.createRow(0);
                String[] headers = {"#", "Usuario", "Nombre", "Apellido", "Correo", "Legajo", "Tipo de usuario", "Carrera", "Rol", "Registrado en"};
                for (int i = 0; i < headers.length; i++) {
                    Cell c = hRow.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                int idx = 1;
                for (Attendance a : attendances) {
                    Row r = sheet.createRow(rowNum++);
                    createDataCell(r, 0, String.valueOf(idx++), dataStyle);
                    createDataCell(r, 1, a.getUser().getUsername(), dataStyle);
                    createDataCell(r, 2, nvl(a.getUser().getFirstName()), dataStyle);
                    createDataCell(r, 3, nvl(a.getUser().getLastName()), dataStyle);
                    createDataCell(r, 4, nvl(a.getUser().getEmail()), dataStyle);
                    createDataCell(r, 5, nvl(a.getUser().getLegajo()), dataStyle);
                    createDataCell(r, 6, nvl(a.getUser().getTipoUsuario()), dataStyle);
                    createDataCell(r, 7, nvl(a.getUser().getCarrera()), dataStyle);
                    createDataCell(r, 8, getUserRoles(a.getUser()), dataStyle);
                    createDataCell(r, 9,
                            a.getRegisteredAt() != null ? a.getRegisteredAt().format(DATE_FMT) : "-",
                            dataStyle);
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            }


            // Fila total en el resumen
            Row totalRow = summary.createRow(summaryRowNum + 1);
            CellStyle totalStyle = createTotalStyle(workbook);
            Cell totalLabel = totalRow.createCell(2);
            totalLabel.setCellValue("TOTAL GLOBAL:");
            totalLabel.setCellStyle(totalStyle);
            Cell totalValue = totalRow.createCell(3);
            totalValue.setCellValue(totalGlobal);
            totalValue.setCellStyle(totalStyle);

            for (int i = 0; i < sh.length; i++) summary.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel global generado: {} reuniones, {} asistencias totales",
                    meetings.size(), totalGlobal);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel global", e);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers de estilo POI
    // -----------------------------------------------------------------------

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(titleFont);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(false);
        return style;
    }

    private CellStyle createTotalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        return style;
    }

    private void createDataCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /** Genera nombre de hoja único: limita a 28 chars + " NNN" para evitar colisiones. */
    private String sanitizeSheetName(String name, int index) {
        if (name == null) name = "Reunion";
        String safe = name.replaceAll("[\\[\\]\\*\\?/\\\\:]", "_");
        // Reservamos 4 chars para el sufijo numérico (e.g. " 01")
        String base = safe.length() > 28 ? safe.substring(0, 28) : safe;
        return String.format("%s %02d", base, index);
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    /** Extrae los nombres de roles del usuario como string separado por coma. */
    private String getUserRoles(com.pic21.domain.User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) return "";
        return user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
