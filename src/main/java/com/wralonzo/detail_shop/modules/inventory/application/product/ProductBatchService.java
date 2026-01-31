package com.wralonzo.detail_shop.modules.inventory.application.product;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ImportReport;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.RowError;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Category;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchConfig;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchPrice;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnit;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductUnitDetails;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.CategoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitRepository;
import com.wralonzo.detail_shop.modules.organization.application.BranchService;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBatchService {

  private final ProductRepository productRepository;
  private final ProductBranchConfigRepository branchConfigRepository;
  private final CategoryRepository categoryRepository;
  private final WarehouseService warehouseService;
  private final BranchService branchService;
  private final ProductUnitRepository productUnitRepository;

  public ImportReport importProductsFromExcel(MultipartFile file) throws Exception {
    UserBusinessContext context = warehouseService.getUserBusinessContext();
    List<RowError> details = new ArrayList<>();
    int created = 0, updated = 0, failed = 0;

    try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
      Sheet sheet = workbook.getSheetAt(0);
      CellStyle errorStyle = createErrorStyle(workbook);

      // Empezamos en 1 para saltar la cabecera
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null || isRowEmpty(row))
          continue;

        try {
          // Llamada única: procesa y nos dice si fue nuevo o no
          boolean isNew = processRowAtomic(row, context);

          if (isNew)
            created++;
          else
            updated++;

        } catch (Exception e) {
          failed++;
          String sku = getCellValueAsString(row.getCell(1));
          details.add(new RowError(i + 1, sku, e.getMessage()));

          markErrorInRow(row, e.getMessage(), errorStyle);
          log.warn("Error en fila {}: {}", i + 1, e.getMessage());
        }
      }

      return ImportReport.builder()
          .created(created)
          .updated(updated)
          .failed(failed)
          .details(details)
          .excelReport(failed > 0 ? generateExcelByteArray(workbook) : null)
          .build();
    }
  }

  @Transactional
  public boolean processRowAtomic(Row row, UserBusinessContext context) {
    String sku = getRequiredString(row, 1, "SKU").toUpperCase();

    // Buscamos el producto en el contexto de la empresa
    Optional<Product> existingProduct = productRepository.findBySkuAndCompanyId(sku, context.companyId());
    boolean isNew = existingProduct.isEmpty();

    Product product = existingProduct.orElseGet(() -> Product.builder()
        .sku(sku)
        .companyId(context.companyId())
        .active(true)
        .build());

    mapMasterFields(product, row);

    // Si es nuevo y no tiene unidades, crear la unidad base por defecto
    if (isNew && (product.getUnits() == null || product.getUnits().isEmpty())) {
      ProductUnit baseUnit = this.productUnitRepository.findById(1L)
          .orElseThrow(() -> new ResourceNotFoundException(
              "Unidad no encontrada ID: 1"));
      ProductUnitDetails productUnitDetails = ProductUnitDetails.builder()
          .product(product)
          .unitProduct(baseUnit)
          .build();
      product.getUnits().add(productUnitDetails);
    }

    product = productRepository.save(product);

    handleBranchAndCategory(product, row, context);

    return isNew;
  }

  private void handleBranchAndCategory(Product product, Row row, UserBusinessContext context) {
    String branchValue = getCellValueAsString(row.getCell(9));
    if (branchValue.isEmpty())
      return;

    Long branchId = parseIdFromDropdown(branchValue);

    String catValue = getCellValueAsString(row.getCell(7));
    Category category = null;
    if (!catValue.isEmpty()) {
      Long catId = parseIdFromDropdown(catValue);
      category = categoryRepository.findById(catId)
          .orElseThrow(() -> new RuntimeException("Categoría ID " + catId + " no existe."));
    }

    ProductBranchConfig config = branchConfigRepository
        .findByProductIdAndBranchId(product.getId(), branchId)
        .orElseGet(() -> ProductBranchConfig.builder()
            .product(product)
            .branchId(branchId)
            .build());

    // config.setSalePrice(product.getBasePrice()); // REMOVED
    config.setStockMinim((int) getCellValueAsDouble(row.getCell(5)));
    config.setCategory(category);

    // Actualizar o crear precio para la unidad base
    final ProductBranchConfig finalConfig = config; // effective final for lambda
    // product.getUnits().stream().filter(u ->
    // u.isBase()).findFirst().ifPresent(baseUnit -> {
    // // Buscar si ya existe precio para esta unidad
    // Optional<ProductBranchPrice> existingPrice = finalConfig.getPrices().stream()
    // .filter(p -> p.getUnit().getId().equals(baseUnit.getId()))
    // .findFirst();

    // if (existingPrice.isPresent()) {
    // existingPrice.get().setPrice(product.getBasePrice());
    // } else {
    // ProductBranchPrice newPrice = ProductBranchPrice.builder()
    // .branchConfig(finalConfig)
    // //.unitProduct(baseUnit)
    // .price(product.getBasePrice())
    // .active(true)
    // .build();
    // finalConfig.getPrices().add(newPrice);
    // }
    // });

    branchConfigRepository.save(config);
  }

  // --- MÉTODOS AUXILIARES ---

  private void mapMasterFields(Product product, Row row) {
    product.setName(getRequiredString(row, 0, "Nombre"));
    product.setDescription(getCellValueAsString(row.getCell(2)));
    product.setPricePurchase(BigDecimal.valueOf(getCellValueAsDouble(row.getCell(3))));
    product.setBasePrice(BigDecimal.valueOf(getCellValueAsDouble(row.getCell(4))));
    product.setBarcode(getCellValueAsString(row.getCell(6)));
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null)
      return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> "";
    };
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

  private Long parseIdFromDropdown(String value) {
    try {
      return Long.parseLong(value.split("-")[0].trim());
    } catch (Exception e) {
      throw new RuntimeException("Formato inválido en selección: " + value);
    }
  }

  private void markErrorInRow(Row row, String message, CellStyle style) {
    Cell errorCell = row.createCell(10);
    errorCell.setCellValue(message);
    errorCell.setCellStyle(style);
  }

  private byte[] generateExcelByteArray(Workbook workbook) throws Exception {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      workbook.write(bos);
      return bos.toByteArray();
    }
  }

  // --- GENERACIÓN DE PLANTILLA ---

  public byte[] generateTemplate() throws Exception {
    UserBusinessContext context = warehouseService.getUserBusinessContext();
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      XSSFSheet sheet = (XSSFSheet) workbook.createSheet("Carga de Productos");

      String[] columns = { "Nombre*", "SKU*", "Descripción", "Precio Compra", "Precio Venta*", "Stock Mínimo",
          "Código Barras", "Categoría (Lista)", "Proveedor", "Sucursal (Lista)*" };

      Row headerRow = sheet.createRow(0);
      CellStyle headerStyle = createHeaderStyle(workbook);
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle);
        sheet.autoSizeColumn(i);
      }

      addDropdown(sheet, context, 9, "sucursales");
      addDropdown(sheet, context, 7, "categorias");

      workbook.write(out);
      return out.toByteArray();
    }
  }

  private void addDropdown(XSSFSheet sheet, UserBusinessContext context, int col, String type) {
    List<String> options = type.equals("sucursales")
        ? branchService.getBranchesByCompany(context.companyId()).stream().map(b -> b.getId() + " - " + b.getName())
            .toList()
        : categoryRepository
            .findByBranchIdInAndDeletedAtIsNull(
                branchService.getBranchesByCompany(context.companyId()).stream().map(Branch::getId).toList())
            .stream().map(c -> c.getId() + " - " + c.getName())
            .toList();

    if (options.isEmpty())
      return;

    DataValidationHelper helper = new XSSFDataValidationHelper(sheet);
    CellRangeAddressList addressList = new CellRangeAddressList(1, 10, col, col);
    DataValidationConstraint constraint = helper.createExplicitListConstraint(options.toArray(new String[0]));
    DataValidation validation = helper.createValidation(constraint, addressList);
    validation.setShowErrorBox(true);
    sheet.addValidationData(validation);
  }

  private CellStyle createHeaderStyle(Workbook wb) {
    CellStyle style = wb.createCellStyle();
    Font font = wb.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
  }

  private CellStyle createErrorStyle(Workbook wb) {
    CellStyle style = wb.createCellStyle();
    style.setFillForegroundColor(IndexedColors.RED.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    Font font = wb.createFont();
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    return style;
  }

  private String getRequiredString(Row row, int index, String fieldName) {
    String val = getCellValueAsString(row.getCell(index));
    if (val.isEmpty())
      throw new RuntimeException(fieldName + " es obligatorio");
    return val;
  }

  private boolean isRowEmpty(Row row) {
    if (row == null)
      return true;
    return getCellValueAsString(row.getCell(0)).isEmpty() && getCellValueAsString(row.getCell(1)).isEmpty();
  }
}