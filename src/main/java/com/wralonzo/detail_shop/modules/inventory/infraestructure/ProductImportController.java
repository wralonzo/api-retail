package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wralonzo.detail_shop.modules.inventory.application.ProductBatchService;
import com.wralonzo.detail_shop.modules.inventory.application.ProductBatchService.ImportReport;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class ProductImportController {

  private final ProductBatchService batchService;

  /**
   * Procesa la carga masiva de productos desde un Excel.
   * Devuelve un reporte detallado con éxitos, errores y advertencias.
   */
  @PostMapping(value = "/products/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportReport> importProducts(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      log.warn("Se intentó realizar una carga masiva con un archivo vacío.");
      return ResponseEntity.badRequest().build();
    }

    try {
      log.info("Iniciando importación masiva: {}", file.getOriginalFilename());
      ImportReport report = batchService.importProductsFromExcel(file);
      return ResponseEntity.ok(report);
    } catch (Exception e) {
      log.error("Error crítico durante la importación: ", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Genera y descarga la plantilla oficial de Excel para la carga de productos.
   */
  @GetMapping("/products/template")
  public void downloadTemplate(HttpServletResponse response) {
    try {
      byte[] excelContent = batchService.generateTemplate();

      // 1. Configuramos los headers directamente en el objeto response
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla_productos.xlsx\"");
      response.setContentLength(excelContent.length);

      // 2. Escribimos los bytes directamente al flujo de salida
      try (var os = response.getOutputStream()) {
        os.write(excelContent);
        os.flush();
      }
      log.info("Plantilla enviada con éxito al cliente.");

    } catch (Exception e) {
      log.error("Error crítico al descargar la plantilla: ", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}