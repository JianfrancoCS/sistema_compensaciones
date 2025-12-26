package com.agropay.core.hiring.service;

import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.constant.FileCategory;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Servicio para generar PDFs de contratos firmados
 * Similar a PayslipPdfService pero adaptado para contratos HTML
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractPdfService implements com.agropay.core.hiring.application.usecase.IContractPdfService {

    private final IInternalFileStorageUseCase internalFileStorageService;
    private final com.agropay.core.hiring.persistence.IContractVariableValueRepository contractVariableValueRepository;

    /**
     * Genera el PDF del contrato con las firmas incluidas y lo guarda como archivo interno
     * 
     * @param contract Entidad del contrato con todas sus variables cargadas
     * @param htmlContent Contenido HTML del contrato con todas las variables ya reemplazadas
     * @param isSigned Indica si el contrato está firmado (true = CONTRACT_SIGNED, false = CONTRACT_INITIAL)
     */
    @Override
    @Transactional
    public void generateAndSaveContractPdf(ContractEntity contract, String htmlContent, boolean isSigned) {
        log.info("Generating PDF for contract: {} (ID: {}), isSigned: {}", contract.getPublicId(), contract.getEntityId(), isSigned);
        
        try {
            // Extraer solo el contenido del body si el HTML tiene estructura completa
            String bodyContent = extractBodyContent(htmlContent);
            log.debug("Body content extracted. Length: {} characters", bodyContent != null ? bodyContent.length() : 0);
            
            // NOTA: El HTML que llega aquí ya debería tener las firmas procesadas como imágenes base64
            // desde getContractContent(), que procesa los placeholders ANTES del merge.
            // Si el HTML aún tiene URLs (por alguna razón), intentar procesarlas.
            // Pero normalmente el HTML ya viene con las imágenes base64 embebidas.
            
            // Verificar si el HTML ya tiene imágenes base64 (data:image)
            boolean hasBase64Images = bodyContent != null && bodyContent.contains("data:image");
            
            String processedHtmlContent;
            if (hasBase64Images) {
                log.debug("HTML ya contiene imágenes base64, no es necesario procesar firmas de nuevo");
                processedHtmlContent = bodyContent;
            } else {
                // Procesar las URLs de las firmas para convertirlas en etiquetas <img>
                processedHtmlContent = processSignatureImages(bodyContent, contract);
                log.debug("Signature images processed. HTML length: {} characters", processedHtmlContent != null ? processedHtmlContent.length() : 0);
            }
            
            // Normalizar el HTML para que sea XML válido (etiquetas auto-cerradas)
            String normalizedHtml = normalizeHtmlForXml(processedHtmlContent);
            log.debug("HTML normalized for XML. Length: {} characters", normalizedHtml != null ? normalizedHtml.length() : 0);
            
            // Convertir HTML a PDF
            log.info("Converting HTML to PDF bytes...");
            byte[] pdfBytes = convertHtmlToPdf(normalizedHtml);
            log.info("PDF bytes generated successfully. Size: {} bytes", pdfBytes != null ? pdfBytes.length : 0);
            
            // Guardar PDF como archivo interno con categoría diferente según estado
            // CONTRACT_INITIAL: PDF sin firma del empleador (generado al crear)
            // CONTRACT_SIGNED: PDF con ambas firmas (generado al firmar)
            String fileName = String.format("contrato_%s_%s.pdf", 
                contract.getContractNumber(), 
                contract.getPublicId().toString().substring(0, 8));
            
            String category = isSigned ? FileCategory.CONTRACT_SIGNED.getCode() : FileCategory.CONTRACT_INITIAL.getCode();
            String description = isSigned ? "Contrato firmado con ambas firmas" : "Contrato inicial (sin firma del empleador)";
            
            log.info("Preparing to save PDF file: fileName={}, category={}, description={}, contractId={}", 
                    fileName, category, description, contract.getId());
            
            // Guardar PDF con categoría específica (no reemplaza el de la otra categoría)
            InternalFileEntity savedFile = internalFileStorageService.saveFile(
                contract,
                pdfBytes,
                fileName,
                "application/pdf",
                category,
                description
            );
            
            log.info("PDF generated and saved successfully for contract: {} (ID: {}) with category: {} and file publicId: {}", 
                    contract.getPublicId(), contract.getEntityId(), category, savedFile.getPublicId());
            
        } catch (Exception e) {
            log.error("ERROR generating PDF for contract {} (ID: {}): {}", 
                    contract.getPublicId(), contract.getEntityId(), e.getMessage(), e);
            log.error("Stack trace completo:", e);
            throw new BusinessValidationException("exception.hiring.contract.pdf-generation-error",
                "Error al generar el PDF del contrato: " + e.getMessage());
        }
    }

    /**
     * Extrae solo el contenido del body del HTML, eliminando estructura HTML completa si existe
     */
    private String extractBodyContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        
        // Si el HTML contiene una etiqueta <body>, extraer solo su contenido
        String lowerContent = htmlContent.toLowerCase();
        int bodyStartIndex = lowerContent.indexOf("<body");
        if (bodyStartIndex >= 0) {
            // Encontrar el inicio del contenido del body (después de >)
            int bodyContentStart = htmlContent.indexOf(">", bodyStartIndex) + 1;
            // Encontrar el cierre de </body>
            int bodyEndIndex = lowerContent.indexOf("</body>", bodyContentStart);
            if (bodyEndIndex > bodyContentStart) {
                return htmlContent.substring(bodyContentStart, bodyEndIndex).trim();
            }
        }
        
        // Si no tiene estructura <body>, devolver el contenido tal cual
        return htmlContent;
    }

    /**
     * Procesa las URLs de las firmas en el HTML y las convierte en etiquetas <img>
     * Convierte URLs de Cloudinary directamente y descarga archivos internos para convertirlos a base64
     * Método público para ser usado tanto en PDFs como en vista previa HTML
     */
    @Override
    public String processSignatureImagesForHtml(String htmlContent, ContractEntity contract) {
        return processSignatureImages(htmlContent, contract);
    }
    
    /**
     * Procesa los placeholders de firmas ANTES de que se reemplacen las variables.
     * Reemplaza directamente {{FIRMA_EMPLEADOR}} y {{FIRMA_ENCARGADO}} con imágenes base64.
     * Esto evita tener que buscar las URLs después de que se reemplacen las variables.
     */
    @Override
    public String processSignaturePlaceholdersBeforeMerge(String templateContent, ContractEntity contract) {
        String processedContent = templateContent;
        
        // Obtener las URLs de las firmas desde las variables
        String firmaEmpleadorUrl = null;
        String firmaEncargadoUrl = null;
        
        // Cargar las variables desde el repositorio
        log.info("Cargando variables para procesar placeholders de firmas antes del merge para contrato {}", contract.getPublicId());
        java.util.List<com.agropay.core.hiring.domain.ContractVariableValueEntity> variableValuesList = 
                contractVariableValueRepository.findByContractId(contract.getEntityId());
        
        // Buscar las firmas en las variables
        if (variableValuesList != null && !variableValuesList.isEmpty()) {
            for (var variableValue : variableValuesList) {
                String code = variableValue.getVariable().getCode();
                String value = variableValue.getValue();
                
                if ("FIRMA_EMPLEADOR".equals(code) && value != null && !value.trim().isEmpty()) {
                    firmaEmpleadorUrl = value;
                    log.info("Firma del empleador encontrada para placeholder: URL={}", firmaEmpleadorUrl);
                } else if ("FIRMA_ENCARGADO".equals(code) && value != null && !value.trim().isEmpty()) {
                    firmaEncargadoUrl = value;
                    log.info("Firma del encargado encontrada para placeholder: URL={}", firmaEncargadoUrl);
                }
            }
        }
        
        // Procesar firma del empleador
        String empleadorPlaceholder = "{{FIRMA_EMPLEADOR}}";
        if (processedContent.contains(empleadorPlaceholder)) {
            String empleadorContent;
            if (firmaEmpleadorUrl != null && !firmaEmpleadorUrl.trim().isEmpty()) {
                String imgSrc = convertSignatureUrlToImageSrc(firmaEmpleadorUrl);
                if (imgSrc != null) {
                    empleadorContent = String.format("<img src=\"%s\" style=\"max-width: 120px; max-height: 80px; width: auto; height: auto; display: block; margin: 10px 0; object-fit: contain;\" />", imgSrc);
                    log.info("Placeholder {{FIRMA_EMPLEADOR}} reemplazado con imagen base64");
                } else {
                    empleadorContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
                    log.warn("No se pudo convertir la firma del empleador a base64. Mostrando espacio vacío.");
                }
            } else {
                empleadorContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
                log.info("Firma del empleador no encontrada. Mostrando espacio vacío para placeholder.");
            }
            processedContent = processedContent.replace(empleadorPlaceholder, empleadorContent);
        }
        
        // Procesar firma del encargado
        String encargadoPlaceholder = "{{FIRMA_ENCARGADO}}";
        if (processedContent.contains(encargadoPlaceholder)) {
            String encargadoContent;
            if (firmaEncargadoUrl != null && !firmaEncargadoUrl.trim().isEmpty()) {
                String imgSrc = convertSignatureUrlToImageSrc(firmaEncargadoUrl);
                if (imgSrc != null) {
                    encargadoContent = String.format("<img src=\"%s\" style=\"max-width: 120px; max-height: 80px; width: auto; height: auto; display: block; margin: 10px 0; object-fit: contain;\" />", imgSrc);
                    log.info("Placeholder {{FIRMA_ENCARGADO}} reemplazado con imagen base64");
                } else {
                    encargadoContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
                    log.warn("No se pudo convertir la firma del encargado a base64. Mostrando espacio vacío.");
                }
            } else {
                encargadoContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
                log.warn("Firma del encargado no encontrada. Mostrando espacio vacío para placeholder.");
            }
            processedContent = processedContent.replace(encargadoPlaceholder, encargadoContent);
        }
        
        return processedContent;
    }
    
    /**
     * Procesa las URLs de las firmas en el HTML y las convierte en etiquetas <img>
     * Convierte URLs de Cloudinary directamente y descarga archivos internos para convertirlos a base64
     */
    private String processSignatureImages(String htmlContent, ContractEntity contract) {
        // Obtener las URLs de las firmas desde las variables
        String firmaEmpleadorUrl = null;
        String firmaEncargadoUrl = null;
        
        // IMPORTANTE: SIEMPRE recargar las variables desde el repositorio para asegurar que estén actualizadas
        // No usar las variables en memoria del objeto contract porque pueden estar desactualizadas
        // (especialmente después de actualizar FIRMA_EMPLEADOR o FIRMA_ENCARGADO)
        log.info("Recargando variables desde repositorio para contrato {} para asegurar datos actualizados", contract.getPublicId());
        java.util.List<com.agropay.core.hiring.domain.ContractVariableValueEntity> variableValuesList = 
                contractVariableValueRepository.findByContractId(contract.getEntityId());
        log.info("Cargadas {} variables desde repositorio para contrato {}", 
                variableValuesList != null ? variableValuesList.size() : 0, contract.getPublicId());
        
        // Buscar las firmas en las variables
        if (variableValuesList != null && !variableValuesList.isEmpty()) {
            log.debug("Procesando {} variables del contrato para buscar firmas", variableValuesList.size());
            for (var variableValue : variableValuesList) {
                String code = variableValue.getVariable().getCode();
                String value = variableValue.getValue();
                
                log.debug("Variable encontrada: code={}, value={}", code, value != null && value.length() > 50 ? value.substring(0, 50) + "..." : value);
                
                if ("FIRMA_EMPLEADOR".equals(code) && value != null && !value.trim().isEmpty()) {
                    firmaEmpleadorUrl = value;
                    log.info("Firma del empleador encontrada en variables: URL={}", firmaEmpleadorUrl);
                } else if ("FIRMA_ENCARGADO".equals(code) && value != null && !value.trim().isEmpty()) {
                    firmaEncargadoUrl = value;
                    log.info("Firma del encargado encontrada en variables: URL={}", firmaEncargadoUrl);
                }
            }
        } else {
            log.warn("No se encontraron variables para contrato {}. No se pueden procesar las firmas.", contract.getPublicId());
        }
        
        log.info("Firmas encontradas - Empleador: {}, Encargado: {}", 
                firmaEmpleadorUrl != null ? "SÍ" : "NO", 
                firmaEncargadoUrl != null ? "SÍ" : "NO");
        
        String processedContent = htmlContent;
        
        // Procesar firma del empleador - siempre mostrar espacio (con imagen si existe, vacío si no)
        String empleadorContent;
        if (firmaEmpleadorUrl != null && !firmaEmpleadorUrl.trim().isEmpty()) {
            log.info("Procesando firma del empleador desde URL: {}", firmaEmpleadorUrl);
            String imgSrc = convertSignatureUrlToImageSrc(firmaEmpleadorUrl);
            if (imgSrc != null) {
                log.info("Firma del empleador convertida exitosamente a base64. Tamaño del data URI: {} caracteres", imgSrc.length());
                // Tamaño de firma: 120px de ancho x 80px de alto
                empleadorContent = String.format("<img src=\"%s\" style=\"max-width: 120px; max-height: 80px; width: auto; height: auto; display: block; margin: 10px 0; object-fit: contain;\" />", imgSrc);
            } else {
                log.warn("No se pudo convertir la firma del empleador a base64. URL: {}", firmaEmpleadorUrl);
                // Si no se pudo convertir, mostrar espacio vacío (mismo tamaño que la imagen)
                empleadorContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
            }
        } else {
            log.info("Firma del empleador no encontrada en las variables del contrato. Mostrando espacio vacío.");
            // Mostrar espacio vacío para la firma del empleador (mismo tamaño que la imagen)
            empleadorContent = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
        }
        processedContent = replaceUrlWithImageTag(processedContent, firmaEmpleadorUrl, empleadorContent, "FIRMA_EMPLEADOR");
        
        // Procesar firma del encargado - siempre debe estar presente
        if (firmaEncargadoUrl != null && !firmaEncargadoUrl.trim().isEmpty()) {
            log.info("Procesando firma del encargado desde URL: {}", firmaEncargadoUrl);
            String imgSrc = convertSignatureUrlToImageSrc(firmaEncargadoUrl);
            if (imgSrc != null) {
                log.info("Firma del encargado convertida exitosamente a base64. Tamaño del data URI: {} caracteres", imgSrc.length());
                // Tamaño de firma: 120px de ancho x 80px de alto
                String imgTag = String.format("<img src=\"%s\" style=\"max-width: 120px; max-height: 80px; width: auto; height: auto; display: block; margin: 10px 0; object-fit: contain;\" />", imgSrc);
                processedContent = replaceUrlWithImageTag(processedContent, firmaEncargadoUrl, imgTag, "FIRMA_ENCARGADO");
            } else {
                log.warn("No se pudo convertir la firma del encargado a base64. URL: {}", firmaEncargadoUrl);
                // Si no se pudo convertir, mostrar espacio vacío
                String emptySpace = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
                processedContent = replaceUrlWithImageTag(processedContent, firmaEncargadoUrl, emptySpace, "FIRMA_ENCARGADO");
            }
        } else {
            log.warn("Firma del encargado no encontrada en las variables del contrato");
            // Si no hay firma del encargado (no debería pasar porque se valida al crear), mostrar espacio vacío
            String emptySpace = "<div style=\"width: 120px; height: 80px; border: 1px dashed #ccc; margin: 10px 0; display: inline-block;\"></div>";
            processedContent = replaceUrlWithImageTag(processedContent, null, emptySpace, "FIRMA_ENCARGADO");
        }
        
        return processedContent;
    }

    /**
     * Reemplaza una URL de firma en el contenido HTML con una etiqueta <img> o espacio vacío
     * Busca la URL de manera flexible, incluso si está dentro de etiquetas HTML o con espacios
     */
    private String replaceUrlWithImageTag(String htmlContent, String url, String replacementContent, String placeholderCode) {
        if (htmlContent == null || replacementContent == null) {
            return htmlContent;
        }
        
        String processed = htmlContent;
        
        // PRIMERO: Siempre buscar y reemplazar el placeholder si aún existe
        // Esto es importante porque populateContent() puede no haber reemplazado todos los placeholders
        String placeholder = "{{" + placeholderCode + "}}";
        if (processed.contains(placeholder)) {
            processed = processed.replace(placeholder, replacementContent);
            log.info("Reemplazado placeholder {} con imagen base64", placeholderCode);
            return processed; // Si encontramos el placeholder, ya está hecho
        }
        
        // SEGUNDO: Si hay URL, buscar y reemplazarla de múltiples formas
        if (url != null && !url.trim().isEmpty()) {
            String trimmedUrl = url.trim();
            boolean replaced = false;
            
            // Método 1: Buscar la URL exacta
            if (processed.contains(trimmedUrl)) {
                processed = processed.replace(trimmedUrl, replacementContent);
                replaced = true;
                log.info("Reemplazada URL exacta {} con imagen base64", trimmedUrl.length() > 60 ? trimmedUrl.substring(0, 60) + "..." : trimmedUrl);
            }
            
            // Método 2: Si no se encontró, buscar variaciones (URL dentro de comillas, etc.)
            if (!replaced) {
                // Escapar caracteres especiales para regex
                String escapedUrl = Pattern.quote(trimmedUrl);
                // Buscar la URL incluso si está dentro de comillas o atributos HTML
                Pattern urlPattern = Pattern.compile(escapedUrl, Pattern.CASE_INSENSITIVE);
                if (urlPattern.matcher(processed).find()) {
                    processed = urlPattern.matcher(processed).replaceAll(Matcher.quoteReplacement(replacementContent));
                    replaced = true;
                    log.info("Reemplazada URL usando regex pattern");
                }
            }
            
            // Método 3: Para URLs internas, buscar solo el publicId
            if (!replaced && trimmedUrl.contains("/v1/internal-files/")) {
                // Extraer el publicId: /v1/internal-files/{publicId}/download
                Pattern publicIdExtractor = Pattern.compile("/v1/internal-files/([a-f0-9-]{36})", Pattern.CASE_INSENSITIVE);
                Matcher matcher = publicIdExtractor.matcher(trimmedUrl);
                if (matcher.find()) {
                    String publicId = matcher.group(1);
                    // Buscar cualquier referencia a este publicId en el HTML
                    if (processed.contains(publicId)) {
                        // Reemplazar toda la URL interna que contenga este publicId
                        Pattern internalUrlPattern = Pattern.compile(
                            Pattern.quote("/v1/internal-files/") + publicId + "[^\"]*",
                            Pattern.CASE_INSENSITIVE
                        );
                        processed = internalUrlPattern.matcher(processed).replaceAll(Matcher.quoteReplacement(replacementContent));
                        replaced = true;
                        log.info("Reemplazada URL interna usando publicId: {}", publicId.substring(0, 8) + "...");
                    }
                }
            }
            
            // Método 4: Para URLs de Cloudinary, buscar el path único
            if (!replaced && trimmedUrl.contains("cloudinary.com")) {
                // Extraer el path después de /image/upload/
                int pathStart = trimmedUrl.indexOf("/image/upload/");
                if (pathStart > 0) {
                    String cloudinaryPath = trimmedUrl.substring(pathStart);
                    // Buscar cualquier referencia a este path
                    Pattern cloudinaryPattern = Pattern.compile(Pattern.quote(cloudinaryPath), Pattern.CASE_INSENSITIVE);
                    if (cloudinaryPattern.matcher(processed).find()) {
                        processed = cloudinaryPattern.matcher(processed).replaceAll(Matcher.quoteReplacement(replacementContent));
                        replaced = true;
                        log.info("Reemplazada URL de Cloudinary usando path");
                    }
                }
            }
            
            // Si aún no se reemplazó, agregar log de advertencia
            if (!replaced) {
                log.warn("No se pudo encontrar ni reemplazar la URL {} para placeholder {}", 
                    trimmedUrl.length() > 60 ? trimmedUrl.substring(0, 60) + "..." : trimmedUrl, 
                    placeholderCode);
            }
        } else {
            // Si no hay URL pero hay placeholder, reemplazar el placeholder con espacio vacío
            processed = processed.replace(placeholder, replacementContent);
            log.debug("Reemplazado placeholder {} con espacio vacío (sin URL)", placeholderCode);
        }
        
        return processed;
    }

    /**
     * Convierte una URL de firma (Cloudinary o interna) a un data URI (base64)
     * Descarga AMBAS imágenes (Cloudinary e internas) y las convierte a base64 para incluirlas en el PDF
     * Similar a cómo lo hace PayslipPdfService
     */
    private String convertSignatureUrlToImageSrc(String signatureUrl) {
        if (signatureUrl == null || signatureUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            log.info("Descargando imagen de firma desde URL: {}", signatureUrl);
            
            // Descargar la imagen como bytes (maneja tanto Cloudinary como archivos internos)
            byte[] imageBytes = downloadImageAsBytes(signatureUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("No se pudo descargar la imagen de firma desde: {}", signatureUrl);
                return null;
            }
            
            // Determinar el MIME type
            String mimeType = determineMimeType(signatureUrl, imageBytes);
            
            // Convertir a base64
            String base64Content = Base64.getEncoder().encodeToString(imageBytes);
            
            // Crear data URI
            String dataUri = String.format("data:%s;base64,%s", mimeType, base64Content);
            log.info("Firma convertida a base64 exitosamente. Tamaño: {} bytes, MIME type: {}", imageBytes.length, mimeType);
            return dataUri;
            
        } catch (Exception e) {
            log.error("Error al convertir URL de firma a base64: {}. Error: {}", signatureUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Descarga una imagen desde una URL (Cloudinary o interna) y la retorna como bytes
     * Similar a PayslipPdfService.downloadImageAsBytes()
     */
    private byte[] downloadImageAsBytes(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        log.info("Descargando imagen desde URL: {}", imageUrl);
        
        // Si es una URL interna (archivo interno), usar el servicio directamente
        if (imageUrl.startsWith("/v1/internal-files/")) {
            try {
                // Extraer el publicId de la URL: /v1/internal-files/{publicId}/download
                String[] parts = imageUrl.split("/");
                if (parts.length >= 4) {
                    String publicIdStr = parts[3];
                    UUID publicId = UUID.fromString(publicIdStr);
                    
                    log.info("URL interna detectada. Obteniendo archivo directamente del servicio. PublicId: {}", publicId);
                    InternalFileEntity file = internalFileStorageService.getFile(publicId);
                    if (file == null) {
                        log.warn("No se encontró el archivo interno. PublicId: {}", publicId);
                        return null;
                    }
                    byte[] fileContent = file.getFileContent();
                    if (fileContent == null || fileContent.length == 0) {
                        log.warn("El archivo interno está vacío. PublicId: {}", publicId);
                        return null;
                    }
                    log.info("Archivo interno obtenido exitosamente. Tamaño: {} bytes, Tipo: {}, Nombre: {}", 
                            fileContent.length, file.getFileType(), file.getFileName());
                    return fileContent;
                } else {
                    log.warn("URL interna con formato inválido: {}", imageUrl);
                    return null;
                }
            } catch (Exception e) {
                log.error("Error al obtener archivo interno desde URL: {}. Error: {}", imageUrl, e.getMessage(), e);
                throw e;
            }
        }
        
        // Si es una URL externa (Cloudinary), descargarla via HTTP
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            try {
                URI uri = URI.create(imageUrl);
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
                    log.info("Imagen de Cloudinary descargada exitosamente. Tamaño: {} bytes, URL: {}", imageBytes.length, imageUrl);
                    return imageBytes;
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                log.error("Error al descargar imagen desde Cloudinary: {}. Error: {}", imageUrl, e.getMessage(), e);
                throw e;
            }
        }
        
        log.warn("URL de imagen con formato desconocido: {}", imageUrl);
        return null;
    }

    /**
     * Determina el MIME type de una imagen basándose en la URL y/o los bytes
     */
    private String determineMimeType(String imageUrl, byte[] imageBytes) {
        // Primero intentar desde la URL
        if (imageUrl != null) {
            String lowerUrl = imageUrl.toLowerCase();
            if (lowerUrl.contains(".png") || lowerUrl.endsWith(".png")) {
                return "image/png";
            } else if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") || lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (lowerUrl.contains(".gif") || lowerUrl.endsWith(".gif")) {
                return "image/gif";
            } else if (lowerUrl.contains(".webp") || lowerUrl.endsWith(".webp")) {
                return "image/webp";
            }
        }
        
        // Si es un archivo interno, verificar desde los bytes (magic numbers)
        if (imageBytes != null && imageBytes.length >= 4) {
            // PNG: 89 50 4E 47
            if (imageBytes[0] == (byte)0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
                return "image/png";
            }
            // JPEG: FF D8 FF
            if (imageBytes[0] == (byte)0xFF && imageBytes[1] == (byte)0xD8 && imageBytes[2] == (byte)0xFF) {
                return "image/jpeg";
            }
            // GIF: 47 49 46 38
            if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46 && imageBytes[3] == 0x38) {
                return "image/gif";
            }
        }
        
        // Default: PNG
        return "image/png";
    }

    /**
     * Normaliza el HTML para que sea XML válido
     * Convierte todas las etiquetas auto-cerradas al formato XML correcto
     */
    private String normalizeHtmlForXml(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        
        String normalized = htmlContent;
        
        // Lista de etiquetas auto-cerradas comunes que necesitan normalización
        String[] selfClosingTags = {"br", "hr", "img", "input", "meta", "link", "area", "base", 
                                     "col", "embed", "source", "track", "wbr"};
        
        // Convertir etiquetas auto-cerradas al formato XML válido
        for (String tag : selfClosingTags) {
            // Caso 1: Reemplazar <tag> (simple, sin atributos) con <tag />
            normalized = normalized.replaceAll("(?i)<" + tag + ">", "<" + tag + " />");
            
            // Caso 2: Reemplazar <tag ...> (con atributos pero sin / antes del >) con <tag ... />
            // Solo reemplazar si no termina ya en />
            normalized = normalized.replaceAll("(?i)<" + tag + "([^/>]+)>", "<" + tag + "$1 />");
        }
        
        return normalized;
    }

    /**
     * Convierte HTML a PDF usando ITextRenderer (Flying Saucer)
     */
    private byte[] convertHtmlToPdf(String htmlContent) {
        try {
            // Envolver el HTML en una estructura completa XHTML válida
            // Nota: Las etiquetas meta deben estar auto-cerradas para XML válido
            StringBuilder fullHtml = new StringBuilder();
            fullHtml.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            fullHtml.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
            fullHtml.append("<head>\n");
            fullHtml.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
            fullHtml.append("    <style>\n");
            fullHtml.append("        body {\n");
            fullHtml.append("            font-family: Arial, sans-serif;\n");
            fullHtml.append("            margin: 40px;\n");
            fullHtml.append("            line-height: 1.6;\n");
            fullHtml.append("        }\n");
            fullHtml.append("        img {\n");
            fullHtml.append("            max-width: 100%;\n");
            fullHtml.append("            height: auto;\n");
            fullHtml.append("        }\n");
            fullHtml.append("        @page {\n");
            fullHtml.append("            margin: 2cm;\n");
            fullHtml.append("        }\n");
            fullHtml.append("    </style>\n");
            fullHtml.append("</head>\n");
            fullHtml.append("<body>\n");
            fullHtml.append(htmlContent != null ? htmlContent : "");
            fullHtml.append("\n</body>\n");
            fullHtml.append("</html>");
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(fullHtml.toString());
            renderer.layout();
            renderer.createPDF(os);
            
            byte[] pdfBytes = os.toByteArray();
            log.info("HTML converted to PDF successfully. PDF size: {} bytes", pdfBytes.length);
            
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            // Log del HTML que causó el error para debugging
            log.debug("HTML content that caused error: {}", htmlContent);
            throw new BusinessValidationException("exception.hiring.contract.html-to-pdf-error", 
                "Error al convertir HTML a PDF: " + e.getMessage());
        }
    }

}

