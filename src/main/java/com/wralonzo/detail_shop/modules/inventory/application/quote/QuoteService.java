package com.wralonzo.detail_shop.modules.inventory.application.quote;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote.QuoteRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote.QuoteResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Quote;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.QuoteRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final WarehouseService warehouseService;

    @Transactional(readOnly = true)
    public Page<QuoteResponse> getAll(Pageable pageable) {
        return quoteRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public QuoteResponse getById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .filter(q -> q.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));
        return mapToResponse(quote);
    }

    @Transactional
    public QuoteResponse create(QuoteRequest request) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();

        Quote quote = Quote.builder()
                .referenceNumber("QUO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(request.getName())
                .prefix("QUO")
                .description(request.getDescription())
                .amount(request.getAmount())
                .discount(request.getDiscount())
                .taxes(request.getTaxes())
                .total(request.getTotal())
                .userId(context.user().getId())
                .warehouseId(request.getWarehouseId())
                .clientId(request.getClientId())
                .status(request.getStatus() != null ? request.getStatus() : Quote.QuoteStatus.DRAFT)
                .notes(request.getNotes())
                .dateExpired(request.getDateExpired())
                .createdBy(context.user().getId())
                .build();

        return mapToResponse(quoteRepository.save(quote));
    }

    @Transactional
    public QuoteResponse update(Long id, QuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));

        UserBusinessContext context = warehouseService.getUserBusinessContext();

        quote.setName(request.getName());
        quote.setDescription(request.getDescription());
        quote.setAmount(request.getAmount());
        quote.setDiscount(request.getDiscount());
        quote.setTaxes(request.getTaxes());
        quote.setTotal(request.getTotal());
        quote.setWarehouseId(request.getWarehouseId());
        quote.setClientId(request.getClientId());
        if (request.getStatus() != null)
            quote.setStatus(request.getStatus());
        quote.setNotes(request.getNotes());
        quote.setDateExpired(request.getDateExpired());
        quote.setUpdatedBy(context.user().getId());

        return mapToResponse(quoteRepository.save(quote));
    }

    @Transactional
    public void delete(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada"));
        quote.setDeletedAt(LocalDateTime.now());
        quoteRepository.save(quote);
    }

    private QuoteResponse mapToResponse(Quote q) {
        return QuoteResponse.builder()
                .id(q.getId())
                .referenceNumber(q.getReferenceNumber())
                .name(q.getName())
                .description(q.getDescription())
                .amount(q.getAmount())
                .discount(q.getDiscount())
                .taxes(q.getTaxes())
                .total(q.getTotal())
                .userId(q.getUserId())
                .warehouseId(q.getWarehouseId())
                .clientId(q.getClientId())
                .status(q.getStatus())
                .notes(q.getNotes())
                .dateExpired(q.getDateExpired())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
