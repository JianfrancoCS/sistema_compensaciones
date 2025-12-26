package com.agropay.core.payroll.service;

import com.agropay.core.address.application.IAddressUseCase;
import com.agropay.core.address.domain.AddressEntity;
import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.persistence.ICompanyRepository;
import com.agropay.core.organization.persistence.ICompanySubsidiarySignerRepository;
import com.agropay.core.organization.domain.CompanySubsidiarySignerEntity;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import com.agropay.core.payroll.domain.enums.DayOfWeekAbbreviation;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * Servicio para generar PDFs de boletas de pago peruanas
 * Genera boletas similares al formato mostrado en la imagen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipPdfService {

    private final IPayrollRepository payrollRepository;
    private final IPayrollDetailRepository payrollDetailRepository;
    private final ICompanyRepository companyRepository;
    private final IAddressUseCase addressService;
    private final IConceptRepository conceptRepository;
    private final ICompanySubsidiarySignerRepository companySubsidiarySignerRepository;
    private final com.agropay.core.files.application.usecase.IInternalFileStorageUseCase internalFileStorageService;
    private final ObjectMapper objectMapper;
 
    @Value("${server.port:10000}")
    private int serverPort;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-PE"));

    /**
     * Genera un PDF de ejemplo con datos mock para visualización
     * Útil para pruebas y verificación del formato
     */
    public byte[] generateExamplePayslipPdf() {
        try {
            // Crear datos mock
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // ============================================
            // HEADER: Información de la empresa (2 columnas: info izquierda, logo derecha)
            // ============================================
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{0.7f, 0.3f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(5);

            // Columna izquierda: Información de la empresa
            Cell leftCell = new Cell();
            leftCell.add(new Paragraph("HASS PERU SA").setFont(fontBold).setFontSize(10));
            leftCell.add(new Paragraph("TELF: 225141").setFont(fontNormal).setFontSize(7));
            leftCell.add(new Paragraph("JR. DIEGO DE ALMAGRO 537").setFont(fontNormal).setFontSize(7));
            leftCell.add(new Paragraph("R.U.C.: 20481121966").setFont(fontNormal).setFontSize(7));
            leftCell.setBorder(Border.NO_BORDER);
            leftCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            headerTable.addCell(leftCell);

            // Columna derecha: Logo de la empresa
            Cell logoCell = new Cell();
            String logoUrl = "https://res.cloudinary.com/dcg6envhf/image/upload/v1763394460/logo_inkasberries_grx8jx.webp";
            try {
                log.info("Intentando cargar logo de la empresa desde URL: {}", logoUrl);
                // Descargar y convertir la imagen a bytes (soporta WebP)
                byte[] imageBytes = downloadImageAsBytes(logoUrl);
                Image logoImage = new Image(ImageDataFactory.create(imageBytes));
                logoImage.setWidth(80); // Ancho del logo en puntos
                logoImage.setAutoScale(true);
                logoCell.add(logoImage);
                log.info("Logo de la empresa cargado exitosamente. Dimensiones: {}x{} puntos", logoImage.getImageWidth(), logoImage.getImageHeight());
            } catch (Exception e) {
                log.error("No se pudo cargar el logo de la empresa desde URL: {}. Error: {}", logoUrl, e.getMessage(), e);
            }
            logoCell.setBorder(Border.NO_BORDER);
            logoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            logoCell.setTextAlignment(TextAlignment.RIGHT);
            headerTable.addCell(logoCell);

            document.add(headerTable);

            // ============================================
            // Título y subtítulo CENTRADOS
            // ============================================
            Paragraph title = new Paragraph("BOLETA DE REMUNERACIONES")
                .setFont(fontBold)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
            document.add(title);

            Paragraph subtitle = new Paragraph("D.S. 001-98 TR - LEY 31110 - OBREROS REG.AGRARIO")
                .setFont(fontNormal)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
            document.add(subtitle);

            Paragraph mes = new Paragraph("MES: AGOSTO 2023")
                .setFont(fontNormal)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2);
            document.add(mes);

            Paragraph weeks = new Paragraph("DE SEMANA: 37 DESDE 31/07/2023 / A SEMANA: 39 HASTA 13/08/2023")
                .setFont(fontNormal)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
            document.add(weeks);

            // ============================================
            // Información del empleado (MOCK) - 2 columnas sin bordes
            // ============================================
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(5);

            // Columna izquierda
            Cell leftInfoCell = new Cell();
            leftInfoCell.add(new Paragraph("NOMBRE:").setFont(fontBold).setFontSize(7));
            leftInfoCell.add(new Paragraph("VERA URIOL YANET MARIBEL").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            leftInfoCell.add(new Paragraph("CARGO:").setFont(fontBold).setFontSize(7));
            leftInfoCell.add(new Paragraph("OBRERO DE CAMPO NIVEL I").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            leftInfoCell.add(new Paragraph("AFILIADO:").setFont(fontBold).setFontSize(7));
            leftInfoCell.add(new Paragraph("INTEGRA - 674580YVUA01").setFont(fontNormal).setFontSize(7));
            leftInfoCell.setBorder(Border.NO_BORDER);
            infoTable.addCell(leftInfoCell);

            // Columna derecha
            Cell rightInfoCell = new Cell();
            rightInfoCell.add(new Paragraph("BÁSICO:").setFont(fontBold).setFontSize(7));
            rightInfoCell.add(new Paragraph("34.17").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            rightInfoCell.add(new Paragraph("CTA. BANCO:").setFont(fontBold).setFontSize(7));
            rightInfoCell.add(new Paragraph("570-72718712-0-97").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            rightInfoCell.add(new Paragraph("BANCO:").setFont(fontBold).setFontSize(7));
            rightInfoCell.add(new Paragraph("BANCO DE CREDITO DEL PERU").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            rightInfoCell.add(new Paragraph("F. INGRESO:").setFont(fontBold).setFontSize(7));
            rightInfoCell.add(new Paragraph("19/10/2021").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
            rightInfoCell.add(new Paragraph("DOCUMENTO:").setFont(fontBold).setFontSize(7));
            rightInfoCell.add(new Paragraph("62806868").setFont(fontNormal).setFontSize(7));
            rightInfoCell.setBorder(Border.NO_BORDER);
            infoTable.addCell(rightInfoCell);

            document.add(infoTable);

            // ============================================
            // TABLA PRINCIPAL: REMUNERACIONES | DESCUENTOS | APORTACIONES | TIEMPOS
            // ============================================
            // Crear una tabla con 4 columnas horizontales CON BORDES
            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(5)
                .setMarginBottom(5);

            // Headers de las 4 columnas CON BORDES
            Cell remunHeader = new Cell().add(new Paragraph("REMUNERACIONES").setFont(fontBold).setFontSize(7));
            mainTable.addHeaderCell(remunHeader);

            Cell descHeader = new Cell().add(new Paragraph("DESCUENTOS").setFont(fontBold).setFontSize(7));
            mainTable.addHeaderCell(descHeader);

            Cell aportHeader = new Cell().add(new Paragraph("APORTACIONES").setFont(fontBold).setFontSize(7));
            mainTable.addHeaderCell(aportHeader);

            Cell tiemposHeader = new Cell().add(new Paragraph("TIEMPOS").setFont(fontBold).setFontSize(7));
            mainTable.addHeaderCell(tiemposHeader);

            // Preparar datos para cada columna
            List<String[]> remunData = Arrays.asList(
                new String[]{"BASICO", "S/.410.04"},
                new String[]{"IMP HOR EXT 25%", "S/.128.14"},
                new String[]{"DESC. SEMANAL", "S/.86.38"},
                new String[]{"BON. X ASIS. SD", "S/.28.00"},
                new String[]{"BONO EXTRAORD.", "S/.5.46"},
                new String[]{"IMP HOR EXT 35%", "S/.18.22"},
                new String[]{"IMP HOR NOCT", "S/.20.93"},
                new String[]{"B PRODUCTI ARAN", "S/.10.24"},
                new String[]{"BONOXMETA", "S/.30.00"},
                new String[]{"BONOXCAMPAÑA", "S/.54.00"},
                new String[]{"BETA", "S/.143.50"},
                new String[]{"CTS_LEY_31110", "S/.39.86"},
                new String[]{"GRAT_LEY_31110", "S/.68.31"}
            );

            List<String[]> descData = Arrays.asList(
                new String[]{"FONDO AFP", "S/.78.60"},
                new String[]{"SEGURO AFP", "S/.14.46"}
            );

            List<String[]> aportData = Arrays.asList(
                new String[]{"ESSALUD", "S/.62.88"},
                new String[]{"VIDA LEY", "S/.3.77"}
            );

            List<String[]> tiemposData = Arrays.asList(
                new String[]{"HORAS NORMALES", "85.16"},
                new String[]{"DIAS TRABAJADOS", "11.00"},
                new String[]{"HORAS_NORMALES", "96.00"},
                new String[]{"HORAS_NOCTURNAS", "14.01"},
                new String[]{"HORAS_EXTRAS", "24.00"},
                new String[]{"HORAS_EXT_INCRE", "3.16"}
            );

            // Encontrar el máximo de filas (sin contar los totales)
            int maxRows = Math.max(Math.max(remunData.size(), descData.size()), 
                                  Math.max(aportData.size(), tiemposData.size()));

            // Agregar filas CON BORDES (sin totales)
            for (int i = 0; i < maxRows; i++) {
                // Columna REMUNERACIONES
                if (i < remunData.size()) {
                    Cell cell = new Cell();
                    // Usar una tabla interna de 2 columnas para alinear texto y número en la misma línea
                    Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER);
                    Cell textCell = new Cell().add(new Paragraph(remunData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    Cell valueCell = new Cell().add(new Paragraph(remunData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    innerTable.addCell(textCell);
                    innerTable.addCell(valueCell);
                    cell.add(innerTable).setPadding(2);
                    mainTable.addCell(cell);
                } else {
                    mainTable.addCell(new Cell());
                }

                // Columna DESCUENTOS
                if (i < descData.size()) {
                    Cell cell = new Cell();
                    Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER);
                    Cell textCell = new Cell().add(new Paragraph(descData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    Cell valueCell = new Cell().add(new Paragraph(descData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    innerTable.addCell(textCell);
                    innerTable.addCell(valueCell);
                    cell.add(innerTable).setPadding(2);
                    mainTable.addCell(cell);
                } else {
                    mainTable.addCell(new Cell());
                }

                // Columna APORTACIONES
                if (i < aportData.size()) {
                    Cell cell = new Cell();
                    Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER);
                    Cell textCell = new Cell().add(new Paragraph(aportData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    Cell valueCell = new Cell().add(new Paragraph(aportData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    innerTable.addCell(textCell);
                    innerTable.addCell(valueCell);
                    cell.add(innerTable).setPadding(2);
                    mainTable.addCell(cell);
                } else {
                    mainTable.addCell(new Cell());
                }

                // Columna TIEMPOS
                if (i < tiemposData.size()) {
                    Cell cell = new Cell();
                    Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setBorder(Border.NO_BORDER);
                    Cell textCell = new Cell().add(new Paragraph(tiemposData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    Cell valueCell = new Cell().add(new Paragraph(tiemposData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(0);
                    innerTable.addCell(textCell);
                    innerTable.addCell(valueCell);
                    cell.add(innerTable).setPadding(2);
                    mainTable.addCell(cell);
                } else {
                    mainTable.addCell(new Cell());
                }
            }

            // Agregar FOOTERS (totales) para cada columna
            Cell remunFooter = new Cell();
            Table remunFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);
            remunFooterTable.addCell(new Cell().add(new Paragraph("TOTAL INGRESOS").setFont(fontBold).setFontSize(6))
                .setBorder(Border.NO_BORDER).setPadding(0));
            remunFooterTable.addCell(new Cell().add(new Paragraph("S/.1043.08").setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(0));
            remunFooter.add(remunFooterTable).setPadding(2);
            mainTable.addFooterCell(remunFooter);

            Cell descFooter = new Cell();
            Table descFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);
            descFooterTable.addCell(new Cell().add(new Paragraph("TOTAL DESCUENTOS").setFont(fontBold).setFontSize(6))
                .setBorder(Border.NO_BORDER).setPadding(0));
            descFooterTable.addCell(new Cell().add(new Paragraph("S/.93.06").setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(0));
            descFooter.add(descFooterTable).setPadding(2);
            mainTable.addFooterCell(descFooter);

            Cell aportFooter = new Cell();
            Table aportFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER);
            aportFooterTable.addCell(new Cell().add(new Paragraph("TOTAL APORTACIÓN").setFont(fontBold).setFontSize(6))
                .setBorder(Border.NO_BORDER).setPadding(0));
            aportFooterTable.addCell(new Cell().add(new Paragraph("S/.66.65").setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(0));
            aportFooter.add(aportFooterTable).setPadding(2);
            mainTable.addFooterCell(aportFooter);

            Cell tiemposFooter = new Cell();
            mainTable.addFooterCell(tiemposFooter);

            document.add(mainTable);

            // ============================================
            // NETO A PAGAR
            // ============================================
            Paragraph neto = new Paragraph("NETO A PAGAR: S/.950.02")
                .setFont(fontBold)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5)
                .setMarginBottom(5);
            document.add(neto);

            // ============================================
            // REGISTRO DIARIO (MOCK) - 4 columnas: DÍA, FECHA, HORAS NORMALES, HORAS EXTRAS
            // ============================================
            Paragraph regTitle = new Paragraph("REGISTRO DIARIO")
                .setFont(fontBold)
                .setFontSize(7)
                .setMarginTop(5)
                .setMarginBottom(3);
            document.add(regTitle);

            Table regTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(5);

            // Headers CON BORDES
            Cell diaHeader = new Cell().add(new Paragraph("DÍA").setFont(fontBold).setFontSize(7));
            regTable.addHeaderCell(diaHeader);
            Cell fechaHeader = new Cell().add(new Paragraph("FECHA").setFont(fontBold).setFontSize(7));
            regTable.addHeaderCell(fechaHeader);
            Cell horasNormHeader = new Cell().add(new Paragraph("HORAS NORMALES").setFont(fontBold).setFontSize(7));
            regTable.addHeaderCell(horasNormHeader);
            Cell horasExtHeader = new Cell().add(new Paragraph("HORAS EXTRAS").setFont(fontBold).setFontSize(7));
            regTable.addHeaderCell(horasExtHeader);

            // Datos de ejemplo (15 días) con horas
            String[][] dailyData = {
                {"LU", "31/07", "8.00", "1.00"},
                {"MA", "01/08", "8.00", "4.00"},
                {"MI", "02/08", "8.00", "5.00"},
                {"JU", "03/08", "8.00", "5.00"},
                {"VI", "04/08", "8.00", "8.00"},
                {"SA", "05/08", "0.00", "0.00"},
                {"DO", "06/08", "0.00", "0.00"},
                {"LU", "07/08", "8.00", "2.50"},
                {"MA", "08/08", "8.00", "7.50"},
                {"MI", "09/08", "8.00", "5.00"},
                {"JU", "10/08", "8.00", "5.00"},
                {"VI", "11/08", "8.00", "3.00"},
                {"SA", "12/08", "0.00", "0.00"},
                {"DO", "13/08", "0.00", "0.00"},
                {"LU", "14/08", "1.00", "0.00"}
            };

            BigDecimal totalHorasNorm = BigDecimal.ZERO;
            BigDecimal totalHorasExt = BigDecimal.ZERO;

            for (String[] day : dailyData) {
                Cell diaCell = new Cell().add(new Paragraph(day[0]).setFont(fontNormal).setFontSize(6));
                regTable.addCell(diaCell);
                Cell fechaCell = new Cell().add(new Paragraph(day[1]).setFont(fontNormal).setFontSize(6));
                regTable.addCell(fechaCell);
                Cell horasNormCell = new Cell().add(new Paragraph(day[2]).setFont(fontNormal).setFontSize(6));
                regTable.addCell(horasNormCell);
                Cell horasExtCell = new Cell().add(new Paragraph(day[3]).setFont(fontNormal).setFontSize(6));
                regTable.addCell(horasExtCell);
                
                totalHorasNorm = totalHorasNorm.add(new BigDecimal(day[2]));
                totalHorasExt = totalHorasExt.add(new BigDecimal(day[3]));
            }

            // Total como footer
            Cell totalDiaCell = new Cell().add(new Paragraph("TOTAL").setFont(fontBold).setFontSize(6));
            regTable.addFooterCell(totalDiaCell);
            Cell totalFechaCell = new Cell().add(new Paragraph(String.valueOf(dailyData.length)).setFont(fontBold).setFontSize(6));
            regTable.addFooterCell(totalFechaCell);
            Cell totalHorasNormCell = new Cell().add(new Paragraph(formatDecimal(totalHorasNorm)).setFont(fontBold).setFontSize(6));
            regTable.addFooterCell(totalHorasNormCell);
            Cell totalHorasExtCell = new Cell().add(new Paragraph(formatDecimal(totalHorasExt)).setFont(fontBold).setFontSize(6));
            regTable.addFooterCell(totalHorasExtCell);

            document.add(regTable);

            // ============================================
            // FOOTER: Firmas (columna izquierda) y QR (columna derecha)
            // ============================================
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{0.6f, 0.4f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(20);

            // Columna izquierda: Firmas (Empleado y Jefe de RRHH)
            Cell signaturesCell = new Cell();
            
            // Firma del empleado
            signaturesCell.add(new Paragraph("_________________________").setFont(fontNormal).setFontSize(8).setMarginTop(10));
            signaturesCell.add(new Paragraph("VERA URIOL YANET MARIBEL").setFont(fontNormal).setFontSize(7).setMarginTop(3));
            signaturesCell.add(new Paragraph("D.N.I.: 62806868").setFont(fontNormal).setFontSize(7).setMarginTop(2));
            signaturesCell.add(new Paragraph("OBRERO DE CAMPO NIVEL I").setFont(fontNormal).setFontSize(7).setMarginTop(2));
            
            // Espacio entre firmas
            signaturesCell.add(new Paragraph(" ").setFont(fontNormal).setFontSize(5).setMarginTop(15));
            
            // Firma del Jefe de RRHH
            signaturesCell.add(new Paragraph("_________________________").setFont(fontNormal).setFontSize(8).setMarginTop(10));
            signaturesCell.add(new Paragraph("LOZANO VASQUEZ, ROY").setFont(fontNormal).setFontSize(7).setMarginTop(3));
            signaturesCell.add(new Paragraph("D.N.I.: 12345678").setFont(fontNormal).setFontSize(7).setMarginTop(2));
            signaturesCell.add(new Paragraph("JEFE DE RECURSOS HUMANOS").setFont(fontNormal).setFontSize(7).setMarginTop(2));
            
            signaturesCell.setBorder(Border.NO_BORDER);
            footerTable.addCell(signaturesCell);

            // Columna derecha: Código QR
            Cell qrCell = new Cell();
            try {
                // Generar código QR con el public_id del detalle de planilla para búsqueda en BD
                // Formato: PAYSLIP:{publicId} - permite búsqueda directa en la BD
                String mockPublicId = UUID.randomUUID().toString();
                String qrData = generateQrData(mockPublicId);
                log.info("Generando código QR para boleta de ejemplo. Public ID: {}", mockPublicId);
                // Generar QR con tamaño mínimo de 200x200 píxeles para garantizar escaneo
                byte[] qrImageBytes = generateQrCode(qrData, 200, 200);
                
                Image qrImage = new Image(ImageDataFactory.create(qrImageBytes));
                qrImage.setWidth(50); // Ancho en puntos PDF (ajustable según diseño)
                qrImage.setAutoScale(true);
                log.info("Código QR generado exitosamente para ejemplo");
                
                qrCell.add(qrImage);
                
                // Fecha de generación y consulta
                LocalDateTime now = LocalDateTime.now();
                Paragraph qrInfo = new Paragraph("Generado el: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(fontNormal)
                    .setFontSize(5)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(3);
                qrCell.add(qrInfo);
                
                // Mostrar solo el UUID sin el prefijo PAYSLIP:
                String displayCode = qrData.replace("PAYSLIP:", "");
                Paragraph qrQuery = new Paragraph("Código: " + displayCode.substring(0, Math.min(20, displayCode.length())) + "...")
                    .setFont(fontNormal)
                    .setFontSize(4)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2);
                qrCell.add(qrQuery);
            } catch (Exception e) {
                log.error("No se pudo generar el código QR: {}", e.getMessage(), e);
            }
            qrCell.setBorder(Border.NO_BORDER);
            qrCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            qrCell.setTextAlignment(TextAlignment.CENTER);
            footerTable.addCell(qrCell);

            document.add(footerTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating example payslip PDF", e);
            throw new BusinessValidationException("exception.payroll.payslip.generation-error", e.getMessage());
        }
    }

    /**
     * Genera el PDF de la boleta de pago para un empleado específico
     */
    public byte[] generatePayslipPdf(UUID payrollPublicId, String employeeDocumentNumber) {
        try {
            PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.payroll.not-found", payrollPublicId.toString()));

            PayrollDetailEntity detail = payrollDetailRepository
                .findByPayrollIdAndEmployeeDocumentNumber(payroll.getId(), employeeDocumentNumber)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.payroll.detail.not-found", employeeDocumentNumber));

            CompanyEntity company = companyRepository.getPrimaryCompany()
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.company.not-found"));

            EmployeeEntity employee = detail.getEmployee();
            PersonEntity person = employee.getPerson();
            PositionEntity position = employee.getPosition();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Fuentes
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Ancho de página A4
            float pageWidth = 595; // A4 width in points
            float margin = 40;

            // ============================================
            // HEADER: Información de la empresa (2 columnas: info izquierda, logo derecha)
            // ============================================
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{0.7f, 0.3f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(5);

            // Columna izquierda: Información de la empresa
            Optional<AddressEntity> primaryAddress = addressService.findPrimaryByEntity(company);
            String addressText = primaryAddress.map(AddressEntity::getAddressText).orElse("");
            String phone = getCompanyPrimaryPhone(company);

            Cell leftCell = new Cell();
            leftCell.add(new Paragraph(company.getTradeName()).setFont(fontBold).setFontSize(10));
            if (!phone.isEmpty() || !addressText.isEmpty()) {
                leftCell.add(new Paragraph("TELF: " + phone + (phone.isEmpty() ? "" : " - ") + addressText)
                    .setFont(fontNormal).setFontSize(7));
            }
            leftCell.add(new Paragraph("R.U.C.: " + company.getRuc()).setFont(fontNormal).setFontSize(7));
            leftCell.setBorder(Border.NO_BORDER);
            leftCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            headerTable.addCell(leftCell);

            // Columna derecha: Logo de la empresa
            Cell logoCell = new Cell();
            if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
                try {
                    log.info("Intentando cargar logo de la empresa desde URL: {}", company.getLogoUrl());
                    // Descargar y convertir la imagen a bytes (soporta WebP)
                    byte[] imageBytes = downloadImageAsBytes(company.getLogoUrl());
                    Image logoImage = new Image(ImageDataFactory.create(imageBytes));
                    logoImage.setWidth(80); // Ancho del logo en puntos
                    logoImage.setAutoScale(true);
                    logoCell.add(logoImage);
                    log.info("Logo de la empresa cargado exitosamente. Dimensiones: {}x{} puntos", logoImage.getImageWidth(), logoImage.getImageHeight());
                } catch (Exception e) {
                    log.error("No se pudo cargar el logo de la empresa desde URL: {}. Error: {}", company.getLogoUrl(), e.getMessage(), e);
                }
            } else {
                log.warn("La empresa no tiene logo_url configurado. Logo no se mostrará en la boleta.");
            }
            logoCell.setBorder(Border.NO_BORDER);
            logoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            logoCell.setTextAlignment(TextAlignment.RIGHT);
            headerTable.addCell(logoCell);

            document.add(headerTable);

            // ============================================
            // Título y subtítulo
            // ============================================
            String monthName = payroll.getPeriodStart().format(MONTH_FORMATTER).toUpperCase();
            
            Paragraph title = new Paragraph("BOLETA DE REMUNERACIONES")
                .setFont(fontBold)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(2);
            document.add(title);

            Paragraph subtitle = new Paragraph("D.S. 001-98 TR - LEY 31110 - OBREROS REG.AGRARIO")
                .setFont(fontNormal)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(3);
            document.add(subtitle);

            Paragraph mes = new Paragraph("MES: " + monthName)
                .setFont(fontNormal)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(2);
            document.add(mes);

            if (payroll.getWeekStart() != null && payroll.getWeekEnd() != null) {
                String weekInfo = String.format("DE SEMANA: %d DESDE %s / A SEMANA: %d HASTA %s",
                    payroll.getWeekStart(),
                    payroll.getPeriodStart().format(DATE_FORMATTER),
                    payroll.getWeekEnd(),
                    payroll.getPeriodEnd().format(DATE_FORMATTER));
                
                Paragraph weeks = new Paragraph(weekInfo)
                    .setFont(fontNormal)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10);
                document.add(weeks);
            }

            // ============================================
            // Información del empleado
            // ============================================
            addEmployeeInfo(document, person, position, employee, fontBold, fontNormal);

            // ============================================
            // TABLA PRINCIPAL: REMUNERACIONES | DESCUENTOS | APORTACIONES | TIEMPOS
            // ============================================
            Map<String, Object> calculatedConcepts = parseCalculatedConcepts(detail.getCalculatedConcepts());
            addMainFinancialTable(document, calculatedConcepts, detail, fontBold, fontNormal);

            // ============================================
            // NETO A PAGAR
            // ============================================
            Paragraph neto = new Paragraph("NETO A PAGAR: " + formatCurrency(detail.getNetToPay()))
                .setFont(fontBold)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5)
                .setMarginBottom(5);
            document.add(neto);

            // ============================================
            // REGISTRO DIARIO (Daily Log)
            // ============================================
            addRegistroDiario(document, detail, fontBold, fontNormal);

            // ============================================
            // FOOTER: Firmas (columna izquierda) y QR (columna derecha)
            // ============================================
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{0.6f, 0.4f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(20);

            // Columna izquierda: Firmas (Empleado y Jefe de RRHH)
            Cell signaturesCell = new Cell();
            
            // Firma del empleado
            signaturesCell.add(new Paragraph("_________________________").setFont(fontNormal).setFontSize(8).setMarginTop(10));
            
            String fullName = String.format("%s %s %s",
                person.getNames(),
                person.getPaternalLastname(),
                person.getMaternalLastname());
            signaturesCell.add(new Paragraph(fullName).setFont(fontNormal).setFontSize(7).setMarginTop(3));
            signaturesCell.add(new Paragraph("D.N.I.: " + person.getDocumentNumber())
                .setFont(fontNormal).setFontSize(7).setMarginTop(2));
            String cargo = position != null ? position.getName() : "N/A";
            signaturesCell.add(new Paragraph(cargo).setFont(fontNormal).setFontSize(7).setMarginTop(2));
            
            // Espacio entre firmas (separación más amplia)
            signaturesCell.add(new Paragraph(" ").setFont(fontNormal).setFontSize(5).setMarginTop(25));
            
            // Firma del Jefe de RRHH
            HrManagerInfo hrManager = getHrManagerInfo(company, employee);
            
            // Agregar imagen de firma si existe
            if (hrManager.signatureImageUrl != null && !hrManager.signatureImageUrl.trim().isEmpty()) {
                try {
                    log.info("Intentando cargar firma del responsable desde URL: {}", hrManager.signatureImageUrl);
                    byte[] signatureImageBytes = downloadImageAsBytes(hrManager.signatureImageUrl);
                    
                    // Verificar si el archivo es SVG (iText no soporta SVG directamente, convertir a PNG)
                    String fileType = getFileTypeFromUrl(hrManager.signatureImageUrl);
                    if (fileType != null && fileType.toLowerCase().contains("svg")) {
                        log.info("Firma en formato SVG detectada. Convirtiendo a PNG... URL: {}", hrManager.signatureImageUrl);
                        try {
                            signatureImageBytes = convertSvgToPng(signatureImageBytes);
                            log.info("Firma SVG convertida exitosamente a PNG. Nuevo tamaño: {} bytes", signatureImageBytes.length);
                        } catch (Exception e) {
                            log.error("Error al convertir firma SVG a PNG: {}", e.getMessage(), e);
                            throw new BusinessValidationException("exception.payroll.payslip.signature.svg-conversion-error", e.getMessage());
                        }
                    }
                    
                    // Intentar crear la imagen
                    Image signatureImage = new Image(ImageDataFactory.create(signatureImageBytes));
                    signatureImage.setWidth(60); // Ancho de la firma en puntos PDF (reducido de 80 a 60)
                    signatureImage.setAutoScale(true);
                    signaturesCell.add(signatureImage.setMarginTop(5));
                    log.info("Firma del responsable agregada exitosamente al PDF. Dimensiones: {}x{} puntos, URL: {}", 
                            signatureImage.getImageWidth(), signatureImage.getImageHeight(), hrManager.signatureImageUrl);
                } catch (BusinessValidationException e) {
                    // Re-lanzar excepciones de negocio sin modificar
                    throw e;
                } catch (Exception e) {
                    log.error("Error al cargar la imagen de firma desde URL: {}. Error completo: {}", 
                            hrManager.signatureImageUrl, e.getMessage(), e);
                    throw new BusinessValidationException("exception.payroll.payslip.signature.load-error", e.getMessage());
                }
            } else {
                log.warn("No hay URL de firma configurada para el responsable. Usando línea de texto.");
                // Si no hay imagen, usar línea de texto
                signaturesCell.add(new Paragraph("_________________________").setFont(fontNormal).setFontSize(8).setMarginTop(10));
            }
            
            signaturesCell.add(new Paragraph(hrManager.fullName).setFont(fontNormal).setFontSize(7).setMarginTop(3));
            signaturesCell.add(new Paragraph("D.N.I.: " + hrManager.documentNumber)
                .setFont(fontNormal).setFontSize(7).setMarginTop(2));
            signaturesCell.add(new Paragraph(hrManager.position).setFont(fontNormal).setFontSize(7).setMarginTop(2));
            
            signaturesCell.setBorder(Border.NO_BORDER);
            footerTable.addCell(signaturesCell);

            // Columna derecha: Código QR
            Cell qrCell = new Cell();
            try {
                // Generar código QR con el public_id del detalle de planilla para búsqueda en BD
                // El QR contiene el publicId del PayrollDetailEntity para búsqueda directa
                String qrData = generateQrData(detail.getPublicId().toString());
                log.info("Generando código QR para boleta. Public ID: {}", detail.getPublicId());
                // Generar QR con tamaño mínimo de 200x200 píxeles para garantizar escaneo
                byte[] qrImageBytes = generateQrCode(qrData, 200, 200);
                
                Image qrImage = new Image(ImageDataFactory.create(qrImageBytes));
                qrImage.setWidth(50); // Ancho en puntos PDF (ajustable según diseño)
                qrImage.setAutoScale(true);
                log.info("Código QR generado exitosamente para boleta");
                
                qrCell.add(qrImage);
                
                // Fecha de generación y consulta
                LocalDateTime now = LocalDateTime.now();
                Paragraph qrInfo = new Paragraph("Generado el: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(fontNormal)
                    .setFontSize(5)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(3);
                qrCell.add(qrInfo);
                
                // Mostrar solo el UUID sin el prefijo PAYSLIP: (truncado para ahorrar espacio)
                String displayCode = qrData.replace("PAYSLIP:", "");
                Paragraph qrQuery = new Paragraph("Código: " + displayCode.substring(0, Math.min(20, displayCode.length())) + "...")
                    .setFont(fontNormal)
                    .setFontSize(4)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2);
                qrCell.add(qrQuery);
            } catch (Exception e) {
                log.error("No se pudo generar el código QR: {}", e.getMessage(), e);
            }
            qrCell.setBorder(Border.NO_BORDER);
            qrCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
            qrCell.setTextAlignment(TextAlignment.CENTER);
            footerTable.addCell(qrCell);

            document.add(footerTable);

            document.close();
            return baos.toByteArray();

        } catch (BusinessValidationException | IdentifierNotFoundException e) {
            // Re-lanzar excepciones de negocio y de recursos no encontrados sin modificar
            throw e;
        } catch (Exception e) {
            log.error("Error generating payslip PDF", e);
            throw new BusinessValidationException("exception.payroll.payslip.generation-error", e.getMessage());
        }
    }

    private void addCompanyHeader(Document document, CompanyEntity company, IAddressUseCase addressService,
                                  PdfFont fontBold, PdfFont fontNormal, float pageWidth, float margin) {
        // Nombre de empresa
        Paragraph companyName = new Paragraph(company.getTradeName())
            .setFont(fontBold)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(3);
        document.add(companyName);

        // Dirección y teléfono de la empresa (usando sistema polimórfico)
        Optional<AddressEntity> primaryAddress = addressService.findPrimaryByEntity(company);
        String addressText = primaryAddress.map(AddressEntity::getAddressText)
            .orElse("");

        // TODO: Obtener teléfono del sistema polimórfico cuando esté implementado
        String phone = "";

        if (!addressText.isEmpty()) {
            Paragraph address = new Paragraph("TELF: " + phone + (phone.isEmpty() ? "" : " - ") + addressText)
                .setFont(fontNormal)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(2);
            document.add(address);
        }

        Paragraph ruc = new Paragraph("R.U.C.: " + company.getRuc())
            .setFont(fontNormal)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(10);
        document.add(ruc);
    }

    private void addPeriodInfo(Document document, PayrollEntity payroll, PdfFont fontBold, PdfFont fontNormal) {
        String monthName = payroll.getPeriodStart().format(MONTH_FORMATTER).toUpperCase();
        
        Paragraph title = new Paragraph("BOLETA DE REMUNERACIONES")
            .setFont(fontBold)
            .setFontSize(11)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(3);
        document.add(title);

        Paragraph period = new Paragraph("PERIODO: " + monthName)
            .setFont(fontNormal)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(2);
        document.add(period);

        Paragraph mes = new Paragraph("MES: " + monthName)
            .setFont(fontNormal)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(2);
        document.add(mes);

        if (payroll.getWeekStart() != null && payroll.getWeekEnd() != null) {
            String weekInfo = String.format("DE SEMANA: %d DESDE %s / A SEMANA: %d HASTA %s",
                payroll.getWeekStart(),
                payroll.getPeriodStart().format(DATE_FORMATTER),
                payroll.getWeekEnd(),
                payroll.getPeriodEnd().format(DATE_FORMATTER));
            
            Paragraph weeks = new Paragraph(weekInfo)
                .setFont(fontNormal)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(10);
            document.add(weeks);
        }
    }

    private void addEmployeeInfo(Document document, PersonEntity person, PositionEntity position,
                                  EmployeeEntity employee, PdfFont fontBold, PdfFont fontNormal) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        String fullName = String.format("%s %s %s",
            person.getNames(),
            person.getPaternalLastname(),
            person.getMaternalLastname());

        // Columna izquierda SIN BORDES
        Cell leftInfoCell = new Cell();
        leftInfoCell.add(new Paragraph("NOMBRE:").setFont(fontBold).setFontSize(7));
        leftInfoCell.add(new Paragraph(fullName).setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        leftInfoCell.add(new Paragraph("CARGO:").setFont(fontBold).setFontSize(7));
        leftInfoCell.add(new Paragraph(position != null ? position.getName() : "N/A").setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        
        // AFP con número de afiliación
        String afpName = getRetirementConceptName(employee);
        String afpAffiliation = employee.getAfpAffiliationNumber() != null ? employee.getAfpAffiliationNumber() : "";
        String afpFull = afpName != null && !afpName.isEmpty() 
            ? (afpName + (afpAffiliation.isEmpty() ? "" : " - " + afpAffiliation))
            : "";
        if (!afpFull.isEmpty()) {
            leftInfoCell.add(new Paragraph("AFILIADO:").setFont(fontBold).setFontSize(7));
            leftInfoCell.add(new Paragraph(afpFull).setFont(fontNormal).setFontSize(7));
        }
        leftInfoCell.setBorder(Border.NO_BORDER);
        infoTable.addCell(leftInfoCell);

        // Columna derecha SIN BORDES
        String basicSalary = employee.getDailyBasicSalary() != null 
            ? formatDecimal(employee.getDailyBasicSalary()) 
            : "";
        Cell rightInfoCell = new Cell();
        rightInfoCell.add(new Paragraph("BÁSICO:").setFont(fontBold).setFontSize(7));
        rightInfoCell.add(new Paragraph(basicSalary).setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        
        String bankAccount = employee.getBankAccountNumber() != null ? employee.getBankAccountNumber() : "";
        rightInfoCell.add(new Paragraph("CTA. BANCO:").setFont(fontBold).setFontSize(7));
        rightInfoCell.add(new Paragraph(bankAccount).setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        
        String bankName = employee.getBankName() != null ? employee.getBankName() : "";
        rightInfoCell.add(new Paragraph("BANCO:").setFont(fontBold).setFontSize(7));
        rightInfoCell.add(new Paragraph(bankName).setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        
        String hireDate = employee.getHireDate() != null 
            ? employee.getHireDate().format(DATE_FORMATTER) 
            : "";
        rightInfoCell.add(new Paragraph("F. INGRESO:").setFont(fontBold).setFontSize(7));
        rightInfoCell.add(new Paragraph(hireDate).setFont(fontNormal).setFontSize(7).setMarginBottom(3));
        
        rightInfoCell.add(new Paragraph("DOCUMENTO:").setFont(fontBold).setFontSize(7));
        rightInfoCell.add(new Paragraph(person.getDocumentNumber()).setFont(fontNormal).setFontSize(7));
        rightInfoCell.setBorder(Border.NO_BORDER);
        infoTable.addCell(rightInfoCell);

        document.add(infoTable);
    }
    
    private void addTableRowTwoColumns(Table table, String leftLabel, String leftValue, 
                                       String rightLabel, String rightValue,
                                       PdfFont labelFont, PdfFont valueFont) {
        // Celda izquierda
        Cell leftCell = new Cell();
        if (!leftLabel.isEmpty()) {
            leftCell.add(new Paragraph(leftLabel).setFont(labelFont));
            leftCell.add(new Paragraph(leftValue).setFont(valueFont));
        }
        leftCell.setBorder(Border.NO_BORDER);
        table.addCell(leftCell);
        
        // Celda derecha
        Cell rightCell = new Cell();
        if (!rightLabel.isEmpty()) {
            rightCell.add(new Paragraph(rightLabel).setFont(labelFont));
            rightCell.add(new Paragraph(rightValue).setFont(valueFont));
        }
        rightCell.setBorder(Border.NO_BORDER);
        table.addCell(rightCell);
    }
    
    private String getRetirementConceptName(EmployeeEntity employee) {
        if (employee.getRetirementConcept() != null) {
            return employee.getRetirementConcept().getName();
        }
        // Fallback: buscar por ID si la relación no está cargada
        if (employee.getRetirementConceptId() != null) {
            return conceptRepository.findById(employee.getRetirementConceptId())
                .map(ConceptEntity::getName)
                .orElse(null);
        }
        return null;
    }
    
    private String getHealthInsuranceConceptName(EmployeeEntity employee) {
        if (employee.getHealthInsuranceConcept() != null) {
            return employee.getHealthInsuranceConcept().getName();
        }
        // Fallback: buscar por ID si la relación no está cargada
        if (employee.getHealthInsuranceConceptId() != null) {
            return conceptRepository.findById(employee.getHealthInsuranceConceptId())
                .map(ConceptEntity::getName)
                .orElse(null);
        }
        return null;
    }

    /**
     * Crea la tabla principal horizontal con 4 columnas: REMUNERACIONES | DESCUENTOS | APORTACIONES | TIEMPOS
     */
    private void addMainFinancialTable(Document document, Map<String, Object> calculatedConcepts,
                                       PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        // Crear una tabla con 4 columnas horizontales CON BORDES
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginTop(5)
            .setMarginBottom(5);

        // Headers de las 4 columnas CON BORDES
        Cell remunHeader = new Cell().add(new Paragraph("REMUNERACIONES").setFont(fontBold).setFontSize(7));
        mainTable.addHeaderCell(remunHeader);

        Cell descHeader = new Cell().add(new Paragraph("DESCUENTOS").setFont(fontBold).setFontSize(7));
        mainTable.addHeaderCell(descHeader);

        Cell aportHeader = new Cell().add(new Paragraph("APORTACIONES").setFont(fontBold).setFontSize(7));
        mainTable.addHeaderCell(aportHeader);

        Cell tiemposHeader = new Cell().add(new Paragraph("TIEMPOS").setFont(fontBold).setFontSize(7));
        mainTable.addHeaderCell(tiemposHeader);

        // Recolectar datos para cada columna
        List<String[]> remunData = new ArrayList<>();
        List<String[]> descData = new ArrayList<>();
        List<String[]> aportData = new ArrayList<>();
        List<String[]> tiemposData = new ArrayList<>();

        // Procesar conceptos calculados
        for (Map.Entry<String, Object> entry : calculatedConcepts.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> conceptData = (Map<String, Object>) entry.getValue();
            String category = (String) conceptData.get("category");
            BigDecimal amount = getBigDecimal(conceptData.get("amount"));
            String conceptName = getConceptDisplayName(entry.getKey());

            com.agropay.core.payroll.domain.enums.ConceptCategoryCode categoryEnum = 
                com.agropay.core.payroll.domain.enums.ConceptCategoryCode.fromCode(category);
            
            if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME) {
                remunData.add(new String[]{conceptName, formatCurrency(amount)});
            } else if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.DEDUCTION 
                    || categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT 
                    || categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYEE_CONTRIBUTION) {
                descData.add(new String[]{conceptName, formatCurrency(amount)});
            } else if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION) {
                aportData.add(new String[]{conceptName, formatCurrency(amount)});
            }
        }

        // Agregar datos de tiempos
        if (detail.getNormalHours() != null) {
            tiemposData.add(new String[]{"HORAS NORMALES", formatDecimal(detail.getNormalHours())});
        }
        if (detail.getDaysWorked() != null) {
            tiemposData.add(new String[]{"DIAS TRABAJADOS", String.valueOf(detail.getDaysWorked())});
        }
        if (detail.getNormalHours() != null) {
            tiemposData.add(new String[]{"HORAS_NORMALES", formatDecimal(detail.getNormalHours())});
        }
        if (detail.getNightHours() != null && detail.getNightHours().compareTo(BigDecimal.ZERO) > 0) {
            tiemposData.add(new String[]{"HORAS_NOCTURNAS", formatDecimal(detail.getNightHours())});
        }
        if (detail.getOvertimeHours25() != null && detail.getOvertimeHours25().compareTo(BigDecimal.ZERO) > 0) {
            tiemposData.add(new String[]{"HORAS_EXTRAS", formatDecimal(detail.getOvertimeHours25())});
        }
        if (detail.getOvertimeHours35() != null && detail.getOvertimeHours35().compareTo(BigDecimal.ZERO) > 0) {
            tiemposData.add(new String[]{"HORAS_EXT_INCRE", formatDecimal(detail.getOvertimeHours35())});
        }

        // Encontrar el máximo de filas (sin contar los totales)
        int maxRows = Math.max(Math.max(remunData.size(), descData.size()), 
                              Math.max(aportData.size(), tiemposData.size()));

        // Agregar filas CON BORDES (sin totales)
        for (int i = 0; i < maxRows; i++) {
            // Columna REMUNERACIONES CON BORDES
            if (i < remunData.size()) {
                Cell cell = new Cell();
                // Usar una tabla interna de 2 columnas para alinear texto y número en la misma línea
                Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);
                Cell textCell = new Cell().add(new Paragraph(remunData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                Cell valueCell = new Cell().add(new Paragraph(remunData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                innerTable.addCell(textCell);
                innerTable.addCell(valueCell);
                cell.add(innerTable).setPadding(2);
                mainTable.addCell(cell);
            } else {
                mainTable.addCell(new Cell());
            }

            // Columna DESCUENTOS CON BORDES
            if (i < descData.size()) {
                Cell cell = new Cell();
                Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);
                Cell textCell = new Cell().add(new Paragraph(descData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                Cell valueCell = new Cell().add(new Paragraph(descData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                innerTable.addCell(textCell);
                innerTable.addCell(valueCell);
                cell.add(innerTable).setPadding(2);
                mainTable.addCell(cell);
            } else {
                mainTable.addCell(new Cell());
            }

            // Columna APORTACIONES CON BORDES
            if (i < aportData.size()) {
                Cell cell = new Cell();
                Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);
                Cell textCell = new Cell().add(new Paragraph(aportData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                Cell valueCell = new Cell().add(new Paragraph(aportData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                innerTable.addCell(textCell);
                innerTable.addCell(valueCell);
                cell.add(innerTable).setPadding(2);
                mainTable.addCell(cell);
            } else {
                mainTable.addCell(new Cell());
            }

            // Columna TIEMPOS CON BORDES
            if (i < tiemposData.size()) {
                Cell cell = new Cell();
                Table innerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setBorder(Border.NO_BORDER);
                Cell textCell = new Cell().add(new Paragraph(tiemposData.get(i)[0]).setFont(fontNormal).setFontSize(6))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                Cell valueCell = new Cell().add(new Paragraph(tiemposData.get(i)[1]).setFont(fontNormal).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);
                innerTable.addCell(textCell);
                innerTable.addCell(valueCell);
                cell.add(innerTable).setPadding(2);
                mainTable.addCell(cell);
            } else {
                mainTable.addCell(new Cell());
            }
        }

        // Agregar FOOTERS (totales) para cada columna
        Cell remunFooter = new Cell();
        Table remunFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(Border.NO_BORDER);
        remunFooterTable.addCell(new Cell().add(new Paragraph("TOTAL INGRESOS").setFont(fontBold).setFontSize(6))
            .setBorder(Border.NO_BORDER).setPadding(0));
        remunFooterTable.addCell(new Cell().add(new Paragraph(formatCurrency(detail.getTotalIncome())).setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(Border.NO_BORDER).setPadding(0));
        remunFooter.add(remunFooterTable).setPadding(2);
        mainTable.addFooterCell(remunFooter);

        Cell descFooter = new Cell();
        Table descFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(Border.NO_BORDER);
        descFooterTable.addCell(new Cell().add(new Paragraph("TOTAL DESCUENTOS").setFont(fontBold).setFontSize(6))
            .setBorder(Border.NO_BORDER).setPadding(0));
        descFooterTable.addCell(new Cell().add(new Paragraph(formatCurrency(detail.getTotalDeductions())).setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(Border.NO_BORDER).setPadding(0));
        descFooter.add(descFooterTable).setPadding(2);
        mainTable.addFooterCell(descFooter);

        Cell aportFooter = new Cell();
        Table aportFooterTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(Border.NO_BORDER);
        aportFooterTable.addCell(new Cell().add(new Paragraph("TOTAL APORTACIÓN").setFont(fontBold).setFontSize(6))
            .setBorder(Border.NO_BORDER).setPadding(0));
        aportFooterTable.addCell(new Cell().add(new Paragraph(formatCurrency(detail.getTotalEmployerContributions())).setFont(fontBold).setFontSize(6).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(Border.NO_BORDER).setPadding(0));
        aportFooter.add(aportFooterTable).setPadding(2);
        mainTable.addFooterCell(aportFooter);

        Cell tiemposFooter = new Cell();
        mainTable.addFooterCell(tiemposFooter);

        document.add(mainTable);
    }

    private void addRemuneracionesSection(Document document, Map<String, Object> calculatedConcepts,
                                          PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph sectionTitle = new Paragraph("REMUNERACIONES")
            .setFont(fontBold)
            .setFontSize(11)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        // Agregar conceptos de ingresos desde calculatedConcepts
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Map.Entry<String, Object> entry : calculatedConcepts.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> conceptData = (Map<String, Object>) entry.getValue();
            String category = (String) conceptData.get("category");
            
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode categoryEnum = 
                com.agropay.core.payroll.domain.enums.ConceptCategoryCode.fromCode(category);
            
            if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME) {
                BigDecimal amount = getBigDecimal(conceptData.get("amount"));
                String conceptName = getConceptDisplayName(entry.getKey());
                addTableRow(table, conceptName, formatCurrency(amount), fontNormal, fontNormal);
                totalIncome = totalIncome.add(amount);
            }
        }

        // Agregar total
        addTableRow(table, "TOTAL INGRESOS", formatCurrency(detail.getTotalIncome()), fontBold, fontBold);

        document.add(table);
    }

    private void addDescuentosSection(Document document, Map<String, Object> calculatedConcepts,
                                       PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph sectionTitle = new Paragraph("DESCUENTOS")
            .setFont(fontBold)
            .setFontSize(11)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        // Agregar conceptos de descuentos
        for (Map.Entry<String, Object> entry : calculatedConcepts.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> conceptData = (Map<String, Object>) entry.getValue();
            String category = (String) conceptData.get("category");
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode categoryEnum = 
                com.agropay.core.payroll.domain.enums.ConceptCategoryCode.fromCode(category);
            
            if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.DEDUCTION 
                    || categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT 
                    || categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYEE_CONTRIBUTION) {
                BigDecimal amount = getBigDecimal(conceptData.get("amount"));
                String conceptName = getConceptDisplayName(entry.getKey());
                addTableRow(table, conceptName, formatCurrency(amount), fontNormal, fontNormal);
            }
        }

        addTableRow(table, "TOTAL DESCUENTOS", formatCurrency(detail.getTotalDeductions()), fontBold, fontBold);

        document.add(table);
    }

    private void addAportacionesSection(Document document, Map<String, Object> calculatedConcepts,
                                         PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph sectionTitle = new Paragraph("APORTACIONES")
            .setFont(fontBold)
            .setFontSize(11)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        // Agregar aportaciones del empleador
        for (Map.Entry<String, Object> entry : calculatedConcepts.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> conceptData = (Map<String, Object>) entry.getValue();
            String category = (String) conceptData.get("category");
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode categoryEnum = 
                com.agropay.core.payroll.domain.enums.ConceptCategoryCode.fromCode(category);
            
            if (categoryEnum == com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION) {
                BigDecimal amount = getBigDecimal(conceptData.get("amount"));
                String conceptName = getConceptDisplayName(entry.getKey());
                addTableRow(table, conceptName, formatCurrency(amount), fontNormal, fontNormal);
            }
        }

        addTableRow(table, "TOTAL APORTACIÓN", formatCurrency(detail.getTotalEmployerContributions()), fontBold, fontBold);

        document.add(table);
    }

    private void addTiemposSection(Document document, PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph sectionTitle = new Paragraph("TIEMPOS")
            .setFont(fontBold)
            .setFontSize(11)
            .setMarginTop(10)
            .setMarginBottom(5);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        if (detail.getNormalHours() != null) {
            addTableRow(table, "HORAS NORMALES", formatDecimal(detail.getNormalHours()), fontNormal, fontNormal);
        }
        if (detail.getDaysWorked() != null) {
            addTableRow(table, "DIAS TRABAJADOS", String.valueOf(detail.getDaysWorked()), fontNormal, fontNormal);
        }
        if (detail.getNormalHours() != null) {
            addTableRow(table, "HORAS_NORMALES", formatDecimal(detail.getNormalHours()), fontNormal, fontNormal);
        }
        if (detail.getNightHours() != null && detail.getNightHours().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(table, "HORAS_NOCTURNAS", formatDecimal(detail.getNightHours()), fontNormal, fontNormal);
        }
        if (detail.getOvertimeHours25() != null && detail.getOvertimeHours25().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(table, "HORAS_EXTRAS", formatDecimal(detail.getOvertimeHours25()), fontNormal, fontNormal);
        }
        if (detail.getOvertimeHours35() != null && detail.getOvertimeHours35().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(table, "HORAS_EXT_INCRE", formatDecimal(detail.getOvertimeHours35()), fontNormal, fontNormal);
        }

        document.add(table);
    }

    private void addNetoAPagar(Document document, PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph neto = new Paragraph("NETO A PAGAR: " + formatCurrency(detail.getNetToPay()))
            .setFont(fontBold)
            .setFontSize(12)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(10)
            .setMarginBottom(15);
        document.add(neto);
    }

    private void addRegistroDiario(Document document, PayrollDetailEntity detail, PdfFont fontBold, PdfFont fontNormal) {
        Paragraph sectionTitle = new Paragraph("REGISTRO DIARIO")
            .setFont(fontBold)
            .setFontSize(7)
            .setMarginTop(5)
            .setMarginBottom(3);
        document.add(sectionTitle);

        List<Map<String, Object>> dailyDetails = parseDailyDetail(detail.getDailyDetail());
        if (dailyDetails.isEmpty()) {
            document.add(new Paragraph("No hay registro diario disponible").setFont(fontNormal).setFontSize(6));
            return;
        }

        // Tabla con 2 columnas: DÍA y FECHA CON BORDES
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(5);

        // Headers CON BORDES
        Cell diaHeader = new Cell().add(new Paragraph("DÍA").setFont(fontBold).setFontSize(7));
        table.addHeaderCell(diaHeader);
        Cell fechaHeader = new Cell().add(new Paragraph("FECHA").setFont(fontBold).setFontSize(7));
        table.addHeaderCell(fechaHeader);

        // Data
        int totalDays = 0;
        
        for (Map<String, Object> dayDetail : dailyDetails) {
            LocalDate date = LocalDate.parse((String) dayDetail.get("date"));
            String dayOfWeek = DayOfWeekAbbreviation.getAbbreviation(date.getDayOfWeek());
            String dateStr = date.format(DATE_FORMATTER);

            Cell diaCell = new Cell().add(new Paragraph(dayOfWeek).setFont(fontNormal).setFontSize(6));
            table.addCell(diaCell);
            Cell fechaCell = new Cell().add(new Paragraph(dateStr).setFont(fontNormal).setFontSize(6));
            table.addCell(fechaCell);
            
            totalDays++;
        }

        // Total
        Cell totalDiaCell = new Cell().add(new Paragraph("TOTAL").setFont(fontBold).setFontSize(6));
        table.addCell(totalDiaCell);
        Cell totalFechaCell = new Cell().add(new Paragraph(String.valueOf(totalDays)).setFont(fontBold).setFontSize(6));
        table.addCell(totalFechaCell);

        document.add(table);
    }

    private void addFooter(Document document, PersonEntity person, PdfFont fontNormal) {
        document.add(new Paragraph("\n").setMarginTop(20));
        
        Paragraph recibido = new Paragraph("RECIBÍ CONFORME")
            .setFont(fontNormal)
            .setFontSize(10)
            .setMarginTop(30);
        document.add(recibido);

        String fullName = String.format("%s %s %s",
            person.getNames(),
            person.getPaternalLastname(),
            person.getMaternalLastname());

        Paragraph name = new Paragraph(fullName)
            .setFont(fontNormal)
            .setFontSize(10)
            .setMarginTop(5);
        document.add(name);

        Paragraph dni = new Paragraph("D.N.I.: " + person.getDocumentNumber())
            .setFont(fontNormal)
            .setFontSize(10)
            .setMarginTop(2);
        document.add(dni);
    }

    // ============================================
    // Helper Methods
    // ============================================

    private void addTableRow(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(labelFont)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(value).setFont(valueFont).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
    }

    private void addTableRow(Table table, String col1, String col2, String col3, String col4, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(col1).setFont(font)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(col2).setFont(font)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(col3).setFont(font).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(col4).setFont(font).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
    }

    private void addTableHeader(Table table, String text, PdfFont font) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setFont(font)).setBorder(Border.NO_BORDER));
    }

    private Map<String, Object> parseCalculatedConcepts(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Error parsing calculated concepts JSON", e);
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> parseDailyDetail(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("Error parsing daily detail JSON", e);
            return new ArrayList<>();
        }
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "S/.0.00";
        return String.format("S/.%.2f", amount.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * Obtiene el nombre de visualización para un concepto en la boleta de pago
     * Usa el enum ConceptCode como única fuente de verdad
     * 
     * @param conceptCode Código del concepto (debe coincidir con ConceptCode enum)
     * @return Nombre formateado para boleta peruana
     */
    private String getConceptDisplayName(String conceptCode) {
        // Usar el enum ConceptCode como única fuente de verdad
        return ConceptCode.getPayslipDisplayName(conceptCode);
    }

    /**
     * Genera los datos para el código QR de la boleta
     * Usa el publicId del PayrollDetailEntity para permitir búsquedas directas en la BD
     * 
     * @param publicId UUID del PayrollDetailEntity (public_id)
     * @return String formateado para el QR: "PAYSLIP:{uuid}"
     */
    private String generateQrData(String publicId) {
        // Formato: PAYSLIP:{publicId}
        // Permite búsqueda directa en la BD usando: WHERE public_id = '{publicId}'
        return String.format("PAYSLIP:%s", publicId);
    }

    /**
     * Genera un código QR como imagen en bytes compatible con escáneres móviles
     * Usa un tamaño mínimo recomendado (200x200 píxeles) para garantizar escaneo correcto
     * 
     * @param data Datos a codificar en el QR
     * @param width Ancho deseado en píxeles (mínimo recomendado: 200)
     * @param height Alto deseado en píxeles (mínimo recomendado: 200)
     * @return Bytes de la imagen PNG del QR code
     */
    private byte[] generateQrCode(String data, int width, int height) throws WriterException, IOException {
        // Configuración para QR code compatible con escáneres móviles
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Nivel M para mejor balance
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); // Margen mínimo de 2 módulos (recomendado por estándar QR)

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        // El encode retorna un BitMatrix con las dimensiones reales del QR
        // Usamos un tamaño mínimo de 200x200 para garantizar legibilidad
        int qrSize = Math.max(Math.max(width, height), 200);
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

        // Obtener dimensiones reales del BitMatrix
        int matrixWidth = bitMatrix.getWidth();
        int matrixHeight = bitMatrix.getHeight();

        // Crear imagen con las dimensiones reales del BitMatrix
        java.awt.image.BufferedImage qrImage = new java.awt.image.BufferedImage(
            matrixWidth, 
            matrixHeight, 
            java.awt.image.BufferedImage.TYPE_INT_RGB
        );
        
        // Rellenar la imagen: negro para módulos activos, blanco para inactivos
        for (int x = 0; x < matrixWidth; x++) {
            for (int y = 0; y < matrixHeight; y++) {
                qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        // Convertir a PNG
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(qrImage, "PNG", pngOutputStream);
        
        log.info("QR code generado: {}x{} píxeles, datos: {}", matrixWidth, matrixHeight, data);
        return pngOutputStream.toByteArray();
    }

    /**
     * Descarga una imagen desde una URL y la convierte a bytes
     * Soporta formatos WebP, PNG, JPG, etc. convirtiéndolos a un formato compatible con iText
     * Para Cloudinary con WebP, convierte automáticamente a PNG usando parámetros de URL
     * Maneja URLs internas usando el servicio de archivos internos directamente (sin HTTP)
     * Maneja URLs relativas convirtiéndolas a absolutas para peticiones externas
     * 
     * @param imageUrl URL de la imagen (puede ser absoluta, relativa o interna)
     * @return Bytes de la imagen en formato compatible (PNG o JPG)
     */
    private byte[] downloadImageAsBytes(String imageUrl) throws IOException {
        log.info("Descargando imagen desde URL: {}", imageUrl);
        
        // Si es una URL interna (archivo interno), usar el servicio directamente
        if (imageUrl.startsWith("/v1/internal-files/")) {
            try {
                // Extraer el publicId de la URL: /v1/internal-files/{publicId}/download
                String[] parts = imageUrl.split("/");
                if (parts.length >= 4) {
                    String publicIdStr = parts[3];
                    java.util.UUID publicId = java.util.UUID.fromString(publicIdStr);
                    
                    log.info("URL interna detectada. Obteniendo archivo directamente del servicio. PublicId: {}", publicId);
                    com.agropay.core.files.domain.InternalFileEntity file = internalFileStorageService.getFile(publicId);
                    if (file == null) {
                        throw new IdentifierNotFoundException("exception.payroll.payslip.image.internal-file-not-found", publicId.toString());
                    }
                    byte[] fileContent = file.getFileContent();
                    if (fileContent == null || fileContent.length == 0) {
                        throw new BusinessValidationException("exception.payroll.payslip.image.internal-file-empty", publicId.toString());
                    }
                    log.info("Archivo interno obtenido exitosamente. Tamaño: {} bytes, Tipo: {}, Nombre: {}", 
                            fileContent.length, file.getFileType(), file.getFileName());
                    return fileContent;
                } else {
                    log.warn("URL interna con formato inválido: {}. Intentando descarga HTTP.", imageUrl);
                }
            } catch (Exception e) {
                log.error("Error al obtener archivo interno desde URL: {}. Error: {}. Intentando descarga HTTP.", imageUrl, e.getMessage(), e);
                // Continuar con descarga HTTP como fallback
            }
        }
        
        String finalUrl = imageUrl;
        
        // Si es una URL relativa (empieza con /), convertirla a absoluta para petición interna
        if (imageUrl.startsWith("/")) {
            finalUrl = "http://localhost:" + serverPort + imageUrl;
            log.info("URL relativa detectada. Convirtiendo a absoluta: {}", finalUrl);
        }
        
        // Si es una URL de Cloudinary con WebP, convertir a PNG usando transformaciones de Cloudinary
        if (finalUrl.contains("cloudinary.com") && finalUrl.toLowerCase().endsWith(".webp")) {
            // Cloudinary: agregar transformación f_png antes del nombre del archivo
            // Formato: .../upload/f_png/v1763394460/logo_inkasberries_grx8jx
            if (finalUrl.contains("/upload/")) {
                int uploadIndex = finalUrl.indexOf("/upload/");
                String baseUrl = finalUrl.substring(0, uploadIndex + "/upload/".length());
                String restOfUrl = finalUrl.substring(uploadIndex + "/upload/".length());
                // Remover .webp del final
                restOfUrl = restOfUrl.replace(".webp", "");
                // Agregar transformación f_png
                finalUrl = baseUrl + "f_png/" + restOfUrl;
            }
            log.info("URL de Cloudinary WebP detectada. Convirtiendo a PNG. URL original: {}, Nueva URL: {}", imageUrl, finalUrl);
        }
        
        java.net.URI uri = java.net.URI.create(finalUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000); // 10 segundos
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] imageBytes = outputStream.toByteArray();
            log.info("Imagen descargada exitosamente. Tamaño: {} bytes, URL final: {}", imageBytes.length, finalUrl);
            
            return imageBytes;
        } catch (BusinessValidationException | IdentifierNotFoundException e) {
            // Re-lanzar excepciones de negocio sin modificar
            throw e;
        } catch (IOException e) {
            log.error("Error al descargar imagen desde URL: {}. Error: {}", finalUrl, e.getMessage(), e);
            throw new BusinessValidationException("exception.payroll.payslip.image.load-error", finalUrl);
        } catch (Exception e) {
            log.error("Error inesperado al descargar imagen desde URL: {}. Error: {}", finalUrl, e.getMessage(), e);
            throw new BusinessValidationException("exception.payroll.payslip.image.load-error", finalUrl);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Convierte un archivo SVG a PNG usando Apache Batik
     * 
     * @param svgBytes Bytes del archivo SVG
     * @return Bytes del archivo PNG convertido
     * @throws TranscoderException Si hay un error en la transcodificación
     * @throws IOException Si hay un error de I/O
     */
    private byte[] convertSvgToPng(byte[] svgBytes) throws TranscoderException, IOException {
        try {
            // Crear transcoder de SVG a PNG
            PNGTranscoder transcoder = new PNGTranscoder();
            
            // Configurar dimensiones (ancho máximo recomendado para firmas: 200px)
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 200f);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 200f);
            
            // Crear input desde los bytes SVG
            String svgContent = new String(svgBytes, java.nio.charset.StandardCharsets.UTF_8);
            TranscoderInput input = new TranscoderInput(new StringReader(svgContent));
            
            // Crear output a un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            
            // Realizar la transcodificación
            transcoder.transcode(input, output);
            
            byte[] pngBytes = outputStream.toByteArray();
            log.info("SVG convertido a PNG exitosamente. Tamaño original: {} bytes, Tamaño PNG: {} bytes", 
                    svgBytes.length, pngBytes.length);
            
            return pngBytes;
        } catch (Exception e) {
            log.error("Error al convertir SVG a PNG: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene el tipo de archivo (MIME type) desde una URL interna
     * 
     * @param imageUrl URL del archivo interno (formato: /v1/internal-files/{publicId}/download)
     * @return MIME type del archivo o null si no se puede obtener
     */
    private String getFileTypeFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/v1/internal-files/")) {
            return null;
        }
        try {
            String[] parts = imageUrl.split("/");
            if (parts.length >= 4) {
                String publicIdStr = parts[3];
                java.util.UUID publicId = java.util.UUID.fromString(publicIdStr);
                com.agropay.core.files.domain.InternalFileEntity file = internalFileStorageService.getFile(publicId);
                if (file != null) {
                    return file.getFileType();
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el tipo de archivo desde URL: {}. Error: {}", imageUrl, e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene el teléfono principal de la empresa desde el sistema polimórfico de teléfonos
     * TODO: Implementar cuando el servicio de teléfonos esté disponible
     * Por ahora retorna cadena vacía
     * 
     * @param company Entidad de la empresa
     * @return Número de teléfono principal o cadena vacía si no existe
     */
    private String getCompanyPrimaryPhone(CompanyEntity company) {
        // TODO: Implementar obtención de teléfono desde sistema polimórfico tb_phones
        // Ejemplo de implementación futura:
        // Optional<PhoneEntity> primaryPhone = phoneService.findPrimaryByEntity(company);
        // return primaryPhone.map(PhoneEntity::getPhoneNumber).orElse("");
        log.warn("Obtención de teléfono de empresa no implementada. Retornando cadena vacía.");
        return "";
    }

    /**
     * Clase interna para almacenar información del jefe de RRHH
     */
    private static class HrManagerInfo {
        final String fullName;
        final String documentNumber;
        final String position;
        final String signatureImageUrl; // URL de la imagen de firma

        HrManagerInfo(String fullName, String documentNumber, String position, String signatureImageUrl) {
            this.fullName = fullName;
            this.documentNumber = documentNumber;
            this.position = position;
            this.signatureImageUrl = signatureImageUrl;
        }
    }

    /**
     * Obtiene la información del jefe de RRHH de la empresa/subsidiaria
     * Busca en la tabla histórica de responsables de firma:
     * 1. Primero busca por subsidiaria del empleado
     * 2. Si no encuentra, busca a nivel de empresa (subsidiary_id = NULL)
     * 3. Si no encuentra nada, lanza una excepción (la planilla no se puede generar sin responsable)
     * 
     * @param company Entidad de la empresa
     * @param employee Entidad del empleado (para obtener la subsidiaria)
     * @return Información del jefe de RRHH
     * @throws BusinessValidationException Si no se encuentra un responsable de firma asignado
     */
    private HrManagerInfo getHrManagerInfo(CompanyEntity company, EmployeeEntity employee) {
        // Obtener la subsidiaria del empleado
        Short subsidiaryId = employee.getSubsidiary() != null ? employee.getSubsidiary().getId() : null;
        String subsidiaryName = employee.getSubsidiary() != null ? employee.getSubsidiary().getName() : "Empresa";
        
        // Buscar responsable más reciente para la subsidiaria específica
        Optional<CompanySubsidiarySignerEntity> signerOpt = companySubsidiarySignerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiaryId);
        
        // Si no encuentra para la subsidiaria, buscar a nivel de empresa
        if (signerOpt.isEmpty() && subsidiaryId != null) {
            signerOpt = companySubsidiarySignerRepository.findLatestByCompany(company.getId());
        }
        
        if (signerOpt.isEmpty()) {
            log.error("No se encontró responsable de firma para empresa {} y subsidiaria {}. La boleta no puede generarse sin un responsable.", 
                    company.getId(), subsidiaryId);
            throw new BusinessValidationException("exception.payroll.launch.missing-signer", subsidiaryName);
        }
        
        CompanySubsidiarySignerEntity signer = signerOpt.get();
        EmployeeEntity responsibleEmployee = signer.getResponsibleEmployee();
        PersonEntity responsiblePerson = responsibleEmployee.getPerson();
        
        // Construir nombre completo
        String fullName = String.format("%s %s, %s",
            responsiblePerson.getPaternalLastname(),
            responsiblePerson.getMaternalLastname(),
            responsiblePerson.getNames());
        
        log.info("Responsable de firma encontrado: {} (Subsidiaria: {})", 
            fullName, subsidiaryId != null ? subsidiaryId : "Nivel Empresa");
        
        return new HrManagerInfo(
            fullName,
            responsiblePerson.getDocumentNumber(),
            signer.getResponsiblePosition(),
            signer.getSignatureImageUrl() // Incluir URL de la firma
        );
    }
}

