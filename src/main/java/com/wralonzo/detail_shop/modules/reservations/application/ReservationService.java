package com.wralonzo.detail_shop.modules.reservations.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories.ClientRepository;
import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryMovementService;
import com.wralonzo.detail_shop.modules.inventory.application.sale.SaleService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleDetailRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationDetailRequest;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationDetailResponse;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationRequest;
import com.wralonzo.detail_shop.modules.reservations.domain.dtos.ReservationResponse;
import com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities.Reservation;
import com.wralonzo.detail_shop.modules.reservations.domain.jpa.entities.ReservationDetail;
import com.wralonzo.detail_shop.modules.reservations.domain.jpa.repositories.ReservationDetailRepository;
import com.wralonzo.detail_shop.modules.reservations.domain.jpa.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final ReservationDetailRepository reservationDetailRepository;
  private final ProductRepository productRepository;
  private final ProductBranchConfigRepository branchConfigRepository;
  private final ClientRepository clientRepository;
  private final InventoryMovementService inventoryMovementService;
  private final WarehouseService warehouseService;
  private final SaleService saleService; // To convert to Sale

  @Transactional
  public ReservationResponse createReservation(ReservationRequest request) {
    UserBusinessContext context = warehouseService.getUserBusinessContext();

    // 1. Validar Cliente
    Client client = clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

    // TODO: Validar límite de crédito si aplica (Corporate Validation)
    // if (client.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) { ... }

    // 2. Crear Cabecera
    Reservation reservation = Reservation.builder()
        .reservationDate(request.getReservationDate() != null ? request.getReservationDate()
            : LocalDate.now())
        .startTime(LocalTime.now())
        .finishDate(LocalTime.now().plusHours(1)) // Placeholder
        .expirationDate(request.getExpirationDate() != null
            ? request.getExpirationDate().atStartOfDay()
            : LocalDate.now().plusDays(3).atStartOfDay())
        .notes(request.getNotes())
        .state(Reservation.Estado.PROGRAMADA)
        .employeeId(context.user().getEmployee().getId())
        .clientId(client.getId())
        .warehouseId(request.getWarehouseId())
        .total(BigDecimal.ZERO)
        .build();

    reservation = reservationRepository.save(reservation);

    BigDecimal total = BigDecimal.ZERO;
    List<ReservationDetailResponse> detailsResponse = new ArrayList<>();

    // 3. Procesar Items y Reservar Stock
    for (ReservationDetailRequest itemReq : request.getItems()) {
      Product product = productRepository.findById(itemReq.getProductId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Producto ID " + itemReq.getProductId() + " no encontrado"));

      // ProductUnit unit = product.getUnits().stream()
      // .filter(u -> u.getId().equals(itemReq.getUnitId()))
      // .findFirst()
      // .orElseThrow(() -> new ResourceNotFoundException("Unidad inválida"));

      // Buscar precio
      ProductBranchConfig branchConfig = branchConfigRepository
          .findByProductIdAndBranchId(product.getId(), request.getWarehouseId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Producto no habilitado en sucursal"));

      // BigDecimal unitPrice = branchConfig.getPrices().stream()
      // .filter(p -> p.getUnit().getId().equals(unit.getId()))
      // .findFirst()
      // .map(ProductBranchPrice::getPrice)
      // .orElseThrow(() -> new ResourceNotFoundException("Precio no configurado"));

      ReservationDetail detail = ReservationDetail.builder()
          .reservation(reservation)
          .product(product)
          // .unit(unit)
          .quantity(itemReq.getQuantity())
          // .priceUnit(unitPrice)
          .build();
      detail.calculateSubtotal();

      reservationDetailRepository.save(detail);

      total = total.add(detail.getSubtotal());
      detailsResponse.add(ReservationDetailResponse.builder()
          .productId(product.getId())
          .productName(product.getName())
          // .unitId(unit.getId())
          // .unitName(unit.getName())
          .quantity(detail.getQuantity())
          .priceUnit(detail.getPriceUnit())
          .subtotal(detail.getSubtotal())
          .build());

      // 4. RESERVAR INVENTARIO
      if (product.getType() == ProductType.BUNDLE) {
        for (ProductBundle component : product.getBundleItems()) {
          int qtyToReserve = itemReq.getQuantity() * component.getQuantity();
          inventoryMovementService.reserveStock(component.getComponentProduct().getId(),
              request.getWarehouseId(), qtyToReserve);
        }
      } else {
        // int baseQty = unit.getConversionFactor()
        // .multiply(BigDecimal.valueOf(itemReq.getQuantity())).intValue();
        inventoryMovementService.reserveStock(product.getId(), request.getWarehouseId(),
            itemReq.getQuantity());
      }
    }

    reservation.setTotal(total);
    reservationRepository.save(reservation);

    return mapToResponse(reservation, detailsResponse);
  }

  @Transactional
  public SaleResponse confirmReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));

    if (reservation.getState() != Reservation.Estado.PROGRAMADA) {
      throw new ResourceConflictException("La reservación no está en estado PROGRAMADA");
    }

    // 1. Convertir a Venta usando SaleService
    List<SaleDetailRequest> saleItems = reservation.getDetails().stream()
        .map(d -> SaleDetailRequest.builder()
            .productId(d.getProduct().getId())
            .unitId(d.getUnit().getId())
            .quantity(d.getQuantity())
            .build())
        .collect(Collectors.toList());

    SaleRequest saleRequest = SaleRequest.builder()
        .clientId(reservation.getClientId())
        .warehouseId(reservation.getWarehouseId())
        .type(Sale.TipoVenta.CONTADO)
        .notes("Confirmación de Reserva #" + reservation.getId())
        .items(saleItems)
        .build();

    // NOTA: SaleService actualmente descuenta stock "real".
    // Como ya tenemos stock "reservado", necesitamos una lógica especial para
    // "confirmar reserva"
    // en lugar de usar SaleService.createSale directamente que intentaría descontar
    // stock disponible (que ya bajó al reservar?).
    // ESPERA: reserveStock SOLO aumenta reservedQuantity. quantityFree = quantity -
    // reservedQuantity.
    // SaleService valida contra quantityFree?
    // -> inventoryMovementService.processSalesMovement chequea "before < quantity".
    // Before es inv.getQuantity().
    // El stock físico NO se ha movido, solo el reservado.
    // PERO SaleService llama a processSalesMovement que hace: if (before <
    // quantity).
    // Si reservé 5 (qty=10, reserved=5, free=5), saleService checkea 10. OK.
    // PERO SaleService resta 5. Queda (qty=5, reserved=5).
    // Deberíamos liberar la reserva TAMBIÉN.

    // SOLUCIÓN: Liberar reserva antes de llamar a createSale?
    // Si libero reserva, alguien más podría ganarlo en ms? (Poco probable en
    // transacción).

    // Paso 1: Liberar Reserva (internamente)
    releaseInventoryForReservation(reservation);

    // Paso 2: Crear Venta (Descuenta stock real)
    SaleResponse saleResponse = saleService.createSale(saleRequest);

    // Paso 3: Actualizar Estado Reserva
    reservation.setState(Reservation.Estado.CONFIRMADA); // O COMPLETADA
    reservationRepository.save(reservation);

    return saleResponse;
  }

  @Transactional
  public void cancelReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));

    if (reservation.getState() != Reservation.Estado.PROGRAMADA) {
      throw new ResourceConflictException("Solo se pueden cancelar reservaciones programadas");
    }

    releaseInventoryForReservation(reservation);

    reservation.setState(Reservation.Estado.CANCELADA);
    reservationRepository.save(reservation);
  }

  private void releaseInventoryForReservation(Reservation reservation) {
    for (ReservationDetail detail : reservation.getDetails()) {
      Product product = detail.getProduct();
      if (product.getType() == ProductType.BUNDLE) {
        for (ProductBundle component : product.getBundleItems()) {
          int qtyToRelease = detail.getQuantity() * component.getQuantity();
          inventoryMovementService.releaseReservedStock(
              component.getComponentProduct().getId(),
              reservation.getWarehouseId(), qtyToRelease);
        }
      } else {
        int baseQty = detail.getUnit().getConversionFactor()
            .multiply(BigDecimal.valueOf(detail.getQuantity()))
            .intValue();
        inventoryMovementService.releaseReservedStock(product.getId(),
            reservation.getWarehouseId(), baseQty);
      }
    }
  }

  private ReservationResponse mapToResponse(Reservation r, List<ReservationDetailResponse> details) {
    return ReservationResponse.builder()
        .id(r.getId())
        .clientId(r.getClientId())
        .warehouseId(r.getWarehouseId())
        .reservationDate(r.getReservationDate())
        .expirationDate(r.getExpirationDate())
        .state(r.getState())
        .total(r.getTotal())
        .notes(r.getNotes())
        .details(details)
        .build();
  }
}
