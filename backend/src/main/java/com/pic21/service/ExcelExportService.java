/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Asistencia
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.AsistenciaRepository
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.service.ExcelExportService
 *  org.apache.poi.ss.usermodel.BorderStyle
 *  org.apache.poi.ss.usermodel.Cell
 *  org.apache.poi.ss.usermodel.CellStyle
 *  org.apache.poi.ss.usermodel.FillPatternType
 *  org.apache.poi.ss.usermodel.Font
 *  org.apache.poi.ss.usermodel.HorizontalAlignment
 *  org.apache.poi.ss.usermodel.IndexedColors
 *  org.apache.poi.ss.usermodel.Row
 *  org.apache.poi.ss.usermodel.Workbook
 *  org.apache.poi.ss.util.CellRangeAddress
 *  org.apache.poi.xssf.usermodel.XSSFSheet
 *  org.apache.poi.xssf.usermodel.XSSFWorkbook
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.Asistencia;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcelExportService {
    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AsistenciaRepository asistenciaRepository;
    private final ReunionRepository reunionRepository;

    @Transactional(readOnly=true)
    public byte[] exportAttendanceByMeeting(Long reunionId) {
        byte[] byArray;
        Reunion reunion = (Reunion)this.reunionRepository.findById((Object)reunionId).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", reunionId));
        List asistencias = this.asistenciaRepository.findByReunionWithDetails(reunion);
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet sheet = workbook.createSheet("Asistencias");
            CellStyle titleStyle = this.createTitleStyle((Workbook)workbook);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Asistencias \u2014 " + reunion.getTitulo());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Reuni\u00f3n:");
            infoRow.createCell(1).setCellValue(reunion.getTitulo());
            Row dateRow = sheet.createRow(2);
            dateRow.createCell(0).setCellValue("Fecha:");
            dateRow.createCell(1).setCellValue(reunion.getFechaInicio() != null ? reunion.getFechaInicio().format(DATE_FMT) : "-");
            Row countRow = sheet.createRow(3);
            countRow.createCell(0).setCellValue("Total asistentes:");
            countRow.createCell(1).setCellValue((double)asistencias.size());
            CellStyle headerStyle = this.createHeaderStyle((Workbook)workbook);
            Row headerRow = sheet.createRow(5);
            String[] headers = new String[]{"#", "Nombre", "Apellido", "Email", "Roles", "DNI / Legajo", "Carrera", "Presente", "Registrado en"};
            for (int i = 0; i < headers.length; ++i) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            CellStyle dataStyle = this.createDataStyle((Workbook)workbook);
            int rowNum = 6;
            int index = 1;
            for (Asistencia a : asistencias) {
                Row row = sheet.createRow(rowNum++);
                Usuario u = a.getUsuario();
                this.createDataCell(row, 0, String.valueOf(index++), dataStyle);
                this.createDataCell(row, 1, this.nvl(u.getNombre()), dataStyle);
                this.createDataCell(row, 2, this.nvl(u.getApellido()), dataStyle);
                this.createDataCell(row, 3, u.getCredencial() != null ? this.nvl(u.getCredencial().getEmail()) : "", dataStyle);
                this.createDataCell(row, 4, this.getUserRoles(u), dataStyle);
                this.createDataCell(row, 5, this.getDocumentoAcademico(u), dataStyle);
                this.createDataCell(row, 6, this.getCarrera(u), dataStyle);
                this.createDataCell(row, 7, a.isPresente() ? "S\u00ed" : "No", dataStyle);
                this.createDataCell(row, 8, a.getFechaRegistro() != null ? a.getFechaRegistro().format(DATE_FMT) : "-", dataStyle);
            }
            for (int i = 0; i < headers.length; ++i) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write((OutputStream)out);
            log.info("Excel generado: {} filas para reuni\u00f3n id={}", (Object)asistencias.size(), (Object)reunionId);
            byArray = out.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    workbook.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new RuntimeException("Error al generar el archivo Excel", e);
            }
        }
        workbook.close();
        return byArray;
    }

    @Transactional(readOnly=true)
    public byte[] exportAllAttendances() {
        byte[] byArray;
        List reuniones = this.reunionRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            XSSFSheet summary = workbook.createSheet("Resumen");
            CellStyle titleStyle = this.createTitleStyle((Workbook)workbook);
            CellStyle headerStyle = this.createHeaderStyle((Workbook)workbook);
            CellStyle dataStyle = this.createDataStyle((Workbook)workbook);
            Row summaryTitle = summary.createRow(0);
            Cell stc = summaryTitle.createCell(0);
            stc.setCellValue("Exportaci\u00f3n Global de Asistencias \u2014 PIC21");
            stc.setCellStyle(titleStyle);
            summary.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            Row summaryHeader = summary.createRow(2);
            String[] sh = new String[]{"Reuni\u00f3n", "Fecha", "Estado", "Total asistentes"};
            for (int i = 0; i < sh.length; ++i) {
                Cell c = summaryHeader.createCell(i);
                c.setCellValue(sh[i]);
                c.setCellStyle(headerStyle);
            }
            int summaryRowNum = 3;
            int totalGlobal = 0;
            int sheetIdx = 1;
            for (Reunion reunion : reuniones) {
                List asistencias = this.asistenciaRepository.findByReunionWithDetails(reunion);
                totalGlobal += asistencias.size();
                Row sRow = summary.createRow(summaryRowNum++);
                this.createDataCell(sRow, 0, reunion.getTitulo(), dataStyle);
                this.createDataCell(sRow, 1, reunion.getFechaInicio() != null ? reunion.getFechaInicio().format(DATE_FMT) : "-", dataStyle);
                this.createDataCell(sRow, 2, reunion.getEstado().name(), dataStyle);
                this.createDataCell(sRow, 3, String.valueOf(asistencias.size()), dataStyle);
                String sheetName = this.sanitizeSheetName(reunion.getTitulo(), sheetIdx++);
                XSSFSheet sheet = workbook.createSheet(sheetName);
                Row hRow = sheet.createRow(0);
                String[] headers = new String[]{"#", "Nombre", "Apellido", "Email", "Roles", "DNI / Legajo", "Carrera", "Presente", "Registrado en"};
                for (int i = 0; i < headers.length; ++i) {
                    Cell c = hRow.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }
                int rowNum = 1;
                int idx = 1;
                for (Asistencia a : asistencias) {
                    Row r = sheet.createRow(rowNum++);
                    Usuario u = a.getUsuario();
                    this.createDataCell(r, 0, String.valueOf(idx++), dataStyle);
                    this.createDataCell(r, 1, this.nvl(u.getNombre()), dataStyle);
                    this.createDataCell(r, 2, this.nvl(u.getApellido()), dataStyle);
                    this.createDataCell(r, 3, u.getCredencial() != null ? this.nvl(u.getCredencial().getEmail()) : "", dataStyle);
                    this.createDataCell(r, 4, this.getUserRoles(u), dataStyle);
                    this.createDataCell(r, 5, this.getDocumentoAcademico(u), dataStyle);
                    this.createDataCell(r, 6, this.getCarrera(u), dataStyle);
                    this.createDataCell(r, 7, a.isPresente() ? "S\u00ed" : "No", dataStyle);
                    this.createDataCell(r, 8, a.getFechaRegistro() != null ? a.getFechaRegistro().format(DATE_FMT) : "-", dataStyle);
                }
                for (int i = 0; i < headers.length; ++i) {
                    sheet.autoSizeColumn(i);
                }
            }
            Row totalRow = summary.createRow(summaryRowNum + 1);
            CellStyle totalStyle = this.createTotalStyle((Workbook)workbook);
            Cell totalLabel = totalRow.createCell(2);
            totalLabel.setCellValue("TOTAL GLOBAL:");
            totalLabel.setCellStyle(totalStyle);
            Cell totalValue = totalRow.createCell(3);
            totalValue.setCellValue((double)totalGlobal);
            totalValue.setCellStyle(totalStyle);
            for (int i = 0; i < sh.length; ++i) {
                summary.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write((OutputStream)out);
            log.info("Excel global generado: {} reuniones, {} asistencias", (Object)reuniones.size(), (Object)totalGlobal);
            byArray = out.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    workbook.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new RuntimeException("Error al generar el archivo Excel global", e);
            }
        }
        workbook.close();
        return byArray;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short)14);
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
        String safe;
        if (name == null) {
            name = "Reunion";
        }
        String base = (safe = name.replaceAll("[\\[\\]\\*\\?/\\\\:]", "_")).length() > 28 ? safe.substring(0, 28) : safe;
        return String.format("%s %02d", base, index);
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    private String getUserRoles(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return "";
        }
        return usuario.getRoles().stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String getCarrera(Usuario usuario) {
        if (usuario == null || usuario.getPerfilEstudiantil() == null) {
            return "";
        }
        String c = usuario.getPerfilEstudiantil().getCarrera();
        return c != null ? c.trim() : "";
    }

    private String getDocumentoAcademico(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
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

    public ExcelExportService(AsistenciaRepository asistenciaRepository, ReunionRepository reunionRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.reunionRepository = reunionRepository;
    }
}

