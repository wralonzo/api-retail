package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wralonzo.detail_shop.modules.inventory.application.product.ProductBatchService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ImportReport;

@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class ProductImportController {
  private final ProductBatchService productBatchService;

  /**
   * Descarga la plantilla de Excel configurada para la empresa del usuario.
   */
  @GetMapping("/template")
  public ResponseEntity<byte[]> downloadTemplate() throws Exception {
    byte[] template = productBatchService.generateTemplate();

    String filename = "plantilla_productos_" + getCurrentTimestamp() + ".xlsx";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(template);
  }

  /**
   * Sube el archivo Excel para procesar los productos.
   * Si hay errores, devuelve el archivo de reporte con las filas marcadas.
   * if (response.status === 206) {
   * const blob = await response.blob();
   * const url = window.URL.createObjectURL(blob);
   * const a = document.createElement('a');
   * a.href = url;
   * a.download = 'errores_importacion.xlsx';
   * a.click();
   * }
   */
  @PostMapping("/import")
  public ResponseEntity<?> importProducts(@RequestParam("file") MultipartFile file) throws Exception {
    ImportReport report = productBatchService.importProductsFromExcel(file);

    // Si hay errores (failed > 0), devolvemos el Excel con los detalles
    if (report.getFailed() > 0) {
      String filename = "reporte_errores_" + getCurrentTimestamp() + ".xlsx";

      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) // 206 Partial Content
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .header("X-Import-Created", String.valueOf(report.getCreated()))
          .header("X-Import-Updated", String.valueOf(report.getUpdated()))
          .header("X-Import-Failed", String.valueOf(report.getFailed()))
          .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(report.getExcelReport());
    }

    // Si todo fue exitoso o solo hubo éxitos, devolvemos JSON con el resumen
    return ResponseEntity.ok(report);
  }

  private String getCurrentTimestamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
  }
  

}