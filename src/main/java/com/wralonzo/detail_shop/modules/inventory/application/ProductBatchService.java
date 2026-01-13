package com.wralonzo.detail_shop.modules.inventory.application;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.SupplierRepository;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBatchService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final SupplierRepository supplierRepository;
  private final WarehouseService warehouseService;

  @Getter
  @Builder
  public static class ImportReport {
    private int created;
    private int updated;
    private int failed;
    private List<String> errorDetails;
    private List<String> warnings;
  }

  public ImportReport importProductsFromExcel(MultipartFile file) throws Exception {
    int createdCount = 0;
    int updatedCount = 0;
    int failedCount = 0;
    List<String> errorDetails = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    Set<String> skusInFile = new HashSet<>();

    try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rows = sheet.iterator();

      if (rows.hasNext())
        rows.next(); // Saltar cabecera

      while (rows.hasNext()) {
        Row row = rows.next();
        int rowNum = row.getRowNum() + 1;

        if (isRowEmpty(row))
          continue;

        try {
          String sku = getCellValueAsString(row.getCell(1));

          if (sku.trim().isEmpty()) {
            failedCount++;
            errorDetails.add("Fila " + rowNum + ": El SKU es obligatorio.");
            continue;
          }

          validateBasicRow(sku, rowNum, skusInFile);
          skusInFile.add(sku);

          // --- LÓGICA DE GUARDADO RESTAURADA ---

          // 1. Buscar si existe o crear nuevo
          Optional<Product> existingProduct = productRepository.findBySku(sku);
          Product product = existingProduct.orElseGet(() -> Product.builder().sku(sku).active(true).build());

          boolean isNew = existingProduct.isEmpty();

          // 2. Mapear datos básicos y Código de barras (Col 0 a 6)
          mapFields(product, row, warnings);

          // 3. Establecer Relaciones (Col 7 a 9)
          setProductRelations(product, row);

          // 4. PERSISTIR EN BASE DE DATOS
          productRepository.save(product);

          if (isNew)
            createdCount++;
          else
            updatedCount++;
          // ---------------------------------------

        } catch (Exception e) {
          failedCount++;
          errorDetails.add("Fila " + rowNum + ": " + e.getMessage());
          log.error("Error en fila {}: {}", rowNum, e.getMessage());
        }
      }
    }
    return ImportReport.builder()
        .created(createdCount).updated(updatedCount).failed(failedCount)
        .errorDetails(errorDetails).warnings(warnings).build();
  }

  private void mapFields(Product product, Row row, List<String> warnings) {
    product.setName(getRequiredString(row, 0, "Nombre"));
    product.setDescription(getCellValueAsString(row.getCell(2)));

    BigDecimal pPurchase = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(3)));
    BigDecimal pSale = BigDecimal.valueOf(getCellValueAsDouble(row.getCell(4)));

    if (pSale.compareTo(pPurchase) < 0) {
      warnings.add("Fila " + (row.getRowNum() + 1) + ": SKU " + product.getSku() + " tiene margen negativo.");
    }

    product.setPricePurchase(pPurchase);
    product.setPriceSale(pSale);
    product.setStockMinim((int) getCellValueAsDouble(row.getCell(5)));
    product.setBarcode(getCellValueAsString(row.getCell(6)));
  }

  private void setProductRelations(Product product, Row row) {
    // CATEGORÍA (Obligatoria) - Columna 7
    String catId = getCellValueAsString(row.getCell(7));
    product.setCategory(categoryRepository.findByCode(catId)
        .orElseThrow(() -> new RuntimeException("Categoría ID " + catId + " no encontrada.")));

    // PROVEEDOR (Opcional) - Columna 8
/*     String supId = getCellValueAsString(row.getCell(8));
    if (supId != null) {
      product.setSupplier(supplierRepository.findByCode(supId)
          .orElseThrow(() -> new RuntimeException("Proveedor ID " + supId + " no encontrado.")));
    }

    // ALMACÉN (Opcional) - Columna 9
    String warId = getCellValueAsString(row.getCell(9));
    if (warId != null) {
      product.setWarehouse(warehouseService.getByCode(warId));
    } */
  }

  // --- MÉTODOS UTILITARIOS ---

  private String getRequiredString(Row row, int index, String fieldName) {
    String val = getCellValueAsString(row.getCell(index));
    if (val.isEmpty())
      throw new RuntimeException(fieldName + " es obligatorio.");
    return val;
  }

  private void validateBasicRow(String sku, int rowNum, Set<String> skusInFile) {
    if (skusInFile.contains(sku))
      throw new RuntimeException("SKU duplicado dentro del archivo: " + sku);
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null)
      return "";
    if (cell.getCellType() == CellType.STRING)
      return cell.getStringCellValue().trim();
    if (cell.getCellType() == CellType.NUMERIC)
      return String.valueOf((long) cell.getNumericCellValue());
    return "";
  }

  private double getCellValueAsDouble(Cell cell) {
    if (cell == null)
      return 0.0;
    try {
      if (cell.getCellType() == CellType.NUMERIC)
        return cell.getNumericCellValue();
      if (cell.getCellType() == CellType.STRING)
        return Double.parseDouble(cell.getStringCellValue());
    } catch (Exception e) {
      return 0.0;
    }
    return 0.0;
  }

  private boolean isRowEmpty(Row row) {
    if (row == null)
      return true;
    String name = getCellValueAsString(row.getCell(0)).trim();
    String sku = getCellValueAsString(row.getCell(1)).trim();
    return name.isEmpty() && sku.isEmpty();
  }

  public byte[] generateTemplate() throws Exception {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Productos");
      String[] columns = { "Nombre", "SKU", "Descripción", "Precio Compra", "Precio Venta", "Stock Mínimo",
          "Código de barras", "COD-CAT-BELLEZA", "COD-PROVEEDOR-BELCORP", "COD-ALMACEN-IPALA" };

      CellStyle headerStyle = workbook.createCellStyle();
      Font font = workbook.createFont();
      font.setBold(true);
      headerStyle.setFont(font);

      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
      }

      // Fila de ejemplo
      Row exampleRow = sheet.createRow(1);
      exampleRow.createCell(0).setCellValue("Producto Ejemplo");
      exampleRow.createCell(1).setCellValue("SKU-001");
      exampleRow.createCell(3).setCellValue(10.0);
      exampleRow.createCell(4).setCellValue(15.0);
      exampleRow.createCell(7).setCellValue(1); // ID Categoría

      workbook.write(out);
      return out.toByteArray();
    }
  }
}