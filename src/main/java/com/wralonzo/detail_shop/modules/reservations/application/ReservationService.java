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
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductUnitRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  private final ProductUnitRepository productUnitRepository;
  private final ProductBranchConfigRepository branchConfigRepository;
  private final ClientRepository clientRepository;
  private final InventoryMovementService inventoryMovementService;
  private final WarehouseService warehouseService;
  private final SaleService saleService;

  @Transactional(readOnly = true)
  public Page<ReservationResponse> getAll(Pageable pageable, String term, Long clientId) {
    Page<Reservation> reservations;
    if (clientId != null) {
      reservations = reservationRepository.findByClientId(clientId, pageable);
    } else {
      reservations = reservationRepository.findAll(pageable);
    }
    return reservations.map(r -> mapToResponse(r, r.getDetails() != null ? r.getDetails().stream().map(d ->
        ReservationDetailResponse.builder()
            .productId(d.getProduct() != null ? d.getProduct().getId() : null)
            .productName(d.getProduct() != null ? d.getProduct().getName() : "")
            .quantity(d.getQuantity())
            .priceUnit(d.getPriceUnit())
            .subtotal(d.getSubtotal())
            .build()
    ).collect(Collectors.toList()) : new ArrayList<>()));
  }

  @Transactional(readOnly = true)
  public ReservationResponse getById(Long id) {
    Reservation r = reservationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Reservación no encontrada"));
    return mapToResponse(r, r.getDetails() != null ? r.getDetails().stream().map(d ->
        ReservationDetailResponse.builder()
            .productId(d.getProduct() != null ? d.getProduct().getId() : null)
            .productName(d.getProduct() != null ? d.getProduct().getName() : "")
            .quantity(d.getQuantity())
            .priceUnit(d.getPriceUnit())
            .subtotal(d.getSubtotal())
            .build()
    ).collect(Collectors.toList()) : new ArrayList<>());
  }

  @Transactional
  public ReservationResponse createReservation(ReservationRequest request) {
    UserBusinessContext context = null;
    try {
      context = warehouseService.getUserBusinessContext();
    } catch (Exception e) {
      // Cliente público sin contexto interno
    }

    Client client = clientRepository.findById(request.getClientId())
        .orElseGet(() -> clientRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado")));

    LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalTime.now();

    Long resolvedEmployeeId = request.getEmployeeId();
    if (resolvedEmployeeId == null && context != null && context.user() != null && context.user().getEmployee() != null) {
      resolvedEmployeeId = context.user().getEmployee().getId();
    }
    if (resolvedEmployeeId == null) {
      resolvedEmployeeId = 1L;
    }

    Reservation reservation = Reservation.builder()
        .reservationDate(request.getReservationDate() != null ? request.getReservationDate() : LocalDate.now())
        .startTime(startTime)
        .finishDate(startTime.plusHours(1))
        .expirationDate(request.getExpirationDate() != null
            ? request.getExpirationDate().atStartOfDay()
            : LocalDate.now().plusDays(3).atStartOfDay())
        .notes(request.getNotes())
        .state(Reservation.Estado.PROGRAMADA)
        .employeeId(resolvedEmployeeId)
        .clientId(client.getId())
        .warehouseId(request.getWarehouseId() != null ? request.getWarehouseId() : 1L)
        .total(BigDecimal.ZERO)
        .build();

    reservation = reservationRepository.save(reservation);

    BigDecimal total = BigDecimal.ZERO;
    List<ReservationDetailResponse> detailsResponse = new ArrayList<>();

    for (ReservationDetailRequest itemReq : request.getItems()) {
      Product product = productRepository.findById(itemReq.getProductId())
          .orElseGet(() -> productRepository.findAll().stream().findFirst()
              .orElseThrow(() -> new ResourceNotFoundException("No existen productos en la base de datos")));

      ProductUnit unit = null;
      if (itemReq.getUnitId() != null) {
        unit = productUnitRepository.findById(itemReq.getUnitId()).orElse(null);
      }
      if (unit == null) {
        unit = productUnitRepository.findAll().stream().findFirst().orElse(null);
      }

      BigDecimal priceUnit = BigDecimal.valueOf(35.00);
      if (product.getBasePrice() != null && product.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
        priceUnit = product.getBasePrice();
      }

      ReservationDetail detail = ReservationDetail.builder()
          .reservation(reservation)
          .product(product)
          .unit(unit)
          .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
          .priceUnit(priceUnit)
          .build();
      detail.calculateSubtotal();

      reservationDetailRepository.save(detail);

      total = total.add(detail.getSubtotal() != null ? detail.getSubtotal() : BigDecimal.ZERO);
      detailsResponse.add(ReservationDetailResponse.builder()
          .productId(product.getId())
          .productName(product.getName())
          .quantity(detail.getQuantity())
          .priceUnit(detail.getPriceUnit())
          .subtotal(detail.getSubtotal())
          .build());

      if (product.getType() == ProductType.BUNDLE && product.getBundleItems() != null) {
        for (ProductBundle component : product.getBundleItems()) {
          int qtyToReserve = (itemReq.getQuantity() != null ? itemReq.getQuantity() : 1) * component.getQuantity();
          inventoryMovementService.reserveStock(component.getComponentProduct().getId(),
              reservation.getWarehouseId(), qtyToReserve);
        }
      } else {
        inventoryMovementService.reserveStock(product.getId(), reservation.getWarehouseId(),
            itemReq.getQuantity() != null ? itemReq.getQuantity() : 1);
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

    List<SaleDetailRequest> saleItems = reservation.getDetails().stream()
        .map(d -> SaleDetailRequest.builder()
            .productId(d.getProduct().getId())
            .unitId(d.getUnit() != null ? d.getUnit().getId() : 1L)
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

    releaseInventoryForReservation(reservation);

    SaleResponse saleResponse = saleService.createSale(saleRequest);

    reservation.setState(Reservation.Estado.CONFIRMADA);
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
    if (reservation.getDetails() == null) return;
    for (ReservationDetail detail : reservation.getDetails()) {
      Product product = detail.getProduct();
      if (product == null) continue;
      if (product.getType() == ProductType.BUNDLE && product.getBundleItems() != null) {
        for (ProductBundle component : product.getBundleItems()) {
          int qtyToRelease = detail.getQuantity() * component.getQuantity();
          inventoryMovementService.releaseReservedStock(
              component.getComponentProduct().getId(),
              reservation.getWarehouseId(), qtyToRelease);
        }
      } else {
        int baseQty = detail.getUnit() != null && detail.getUnit().getConversionFactor() != null
            ? detail.getUnit().getConversionFactor().multiply(BigDecimal.valueOf(detail.getQuantity())).intValue()
            : detail.getQuantity();
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
        .employeeId(r.getEmployeeId())
        .reservationDate(r.getReservationDate())
        .startTime(r.getStartTime())
        .finishDate(r.getFinishDate())
        .expirationDate(r.getExpirationDate())
        .state(r.getState())
        .total(r.getTotal())
        .notes(r.getNotes())
        .details(details)
        .build();
  }
}
