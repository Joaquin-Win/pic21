package com.pic21.service;

import com.pic21.domain.Asistencia;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
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
 * Servicio de exportación Excel (UML v8).
 * Genera reportes de asistencias por reunión o globales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AsistenciaRepository asistenciaRepository;
    private final ReunionRepository reunionRepository;

    @Transactional(readOnly = true)
    public byte[] exportAttendanceByMeeting(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", reunionId));

        List<Asistencia> asistencias = asistenciaRepository.findByReunionWithDetails(reunion);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Asistencias");

            CellStyle titleStyle = createTitleStyle(workbook);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Asistencias — " + reunion.getTitulo());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Reunión:");
            infoRow.createCell(1).setCellValue(reunion.getTitulo());
            Row dateRow = sheet.createRow(2);
            dateRow.createCell(0).setCellValue("Fecha:");
            dateRow.createCell(1).setCellValue(
                    reunion.getFechaInicio() != null ? reunion.getFechaInicio().format(DATE_FMT) : "-");
            Row countRow = sheet.createRow(3);
            countRow.createCell(0).setCellValue("Total asistentes:");
            countRow.createCell(1).setCellValue(asistencias.size());

            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(5);
            String[] headers = {"#", "Nombre", "Apellido", "Email", "Roles", "DNI / Legajo", "Carrera", "Presente", "Registrado en"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 6, index = 1;
            for (Asistencia a : asistencias) {
                Row row = sheet.createRow(rowNum++);
                Usuario u = a.getUsuario();
                createDataCell(row, 0, String.valueOf(index++), dataStyle);
                createDataCell(row, 1, nvl(u.getNombre()), dataStyle);
                createDataCell(row, 2, nvl(u.getApellido()), dataStyle);
                createDataCell(row, 3, u.getCredencial() != null ? nvl(u.getCredencial().getEmail()) : "", dataStyle);
                createDataCell(row, 4, getUserRoles(u), dataStyle);
                createDataCell(row, 5, getDocumentoAcademico(u), dataStyle);
                createDataCell(row, 6, getCarrera(u), dataStyle);
                createDataCell(row, 7, a.isPresente() ? "Sí" : "No", dataStyle);
                createDataCell(row, 8,
                        a.getFechaRegistro() != null ? a.getFechaRegistro().format(DATE_FMT) : "-", dataStyle);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel generado: {} filas para reunión id={}", asistencias.size(), reunionId);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportAllAttendances() {
        List<Reunion> reuniones = reunionRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
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

            int summaryRowNum = 3, totalGlobal = 0, sheetIdx = 1;

            for (Reunion reunion : reuniones) {
                List<Asistencia> asistencias = asistenciaRepository.findByReunionWithDetails(reunion);
                totalGlobal += asistencias.size();

                Row sRow = summary.createRow(summaryRowNum++);
                createDataCell(sRow, 0, reunion.getTitulo(), dataStyle);
                createDataCell(sRow, 1,
                        reunion.getFechaInicio() != null ? reunion.getFechaInicio().format(DATE_FMT) : "-", dataStyle);
                createDataCell(sRow, 2, reunion.getEstado().name(), dataStyle);
                createDataCell(sRow, 3, String.valueOf(asistencias.size()), dataStyle);

                String sheetName = sanitizeSheetName(reunion.getTitulo(), sheetIdx++);
                Sheet sheet = workbook.createSheet(sheetName);

                Row hRow = sheet.createRow(0);
                String[] headers = {"#", "Nombre", "Apellido", "Email", "Roles", "DNI / Legajo", "Carrera", "Presente", "Registrado en"};
                for (int i = 0; i < headers.length; i++) {
                    Cell c = hRow.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                int rowNum = 1, idx = 1;
                for (Asistencia a : asistencias) {
                    Row r = sheet.createRow(rowNum++);
                    Usuario u = a.getUsuario();
                    createDataCell(r, 0, String.valueOf(idx++), dataStyle);
                    createDataCell(r, 1, nvl(u.getNombre()), dataStyle);
                    createDataCell(r, 2, nvl(u.getApellido()), dataStyle);
                    createDataCell(r, 3, u.getCredencial() != null ? nvl(u.getCredencial().getEmail()) : "", dataStyle);
                    createDataCell(r, 4, getUserRoles(u), dataStyle);
                    createDataCell(r, 5, getDocumentoAcademico(u), dataStyle);
                    createDataCell(r, 6, getCarrera(u), dataStyle);
                    createDataCell(r, 7, a.isPresente() ? "Sí" : "No", dataStyle);
                    createDataCell(r, 8,
                            a.getFechaRegistro() != null ? a.getFechaRegistro().format(DATE_FMT) : "-", dataStyle);
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            }

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
            log.info("Excel global generado: {} reuniones, {} asistencias", reuniones.size(), totalGlobal);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel global", e);
        }
    }

    // ── Helpers POI ──────────────────────────────────────────────────────────

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
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

    private String sanitizeSheetName(String name, int index) {
        if (name == null) name = "Reunion";
        String safe = name.replaceAll("[\\[\\]\\*\\?/\\\\:]", "_");
        String base = safe.length() > 28 ? safe.substring(0, 28) : safe;
        return String.format("%s %02d", base, index);
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    private String getUserRoles(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) return "";
        return usuario.getRoles().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /** Carrera del perfil estudiantil (estudiantes y ayudantes); vacío para el resto. */
    private String getCarrera(Usuario usuario) {
        if (usuario == null || usuario.getPerfilEstudiantil() == null) return "";
        String c = usuario.getPerfilEstudiantil().getCarrera();
        return c != null ? c.trim() : "";
    }

    /** Muestra DNI (grupo A) o Legajo (grupo B) según perfil disponible. */
    private String getDocumentoAcademico(Usuario usuario) {
        if (usuario == null) return "";
        if (usuario.getPerfilEstudiantil() != null) {
            String legajo = usuario.getPerfilEstudiantil().getLegajo();
            return legajo != null ? legajo.trim() : "";
        }
        if (usuario.getPerfilPersonal() != null) {
            String dni = usuario.getPerfilPersonal().getDni();
            return dni != null ? dni.trim() : "";
        }
        return "";
    }
}
