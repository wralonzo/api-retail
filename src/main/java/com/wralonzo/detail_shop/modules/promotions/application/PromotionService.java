package com.wralonzo.detail_shop.modules.promotions.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.promotions.domain.dtos.PromotionRequest;
import com.wralonzo.detail_shop.modules.promotions.domain.dtos.PromotionResponse;
import com.wralonzo.detail_shop.modules.promotions.domain.jpa.entities.Promotion;
import com.wralonzo.detail_shop.modules.promotions.domain.jpa.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        List<Promotion> promotions = promotionRepository.findActivePromotions(LocalDate.now());
        if (promotions.isEmpty()) {
            return List.of(
                PromotionResponse.builder()
                    .id(1L)
                    .title("2x1 en Hidratación Capilar")
                    .description("Por la compra de cualquier servicio de coloración, obtén tratamiento hidratante gratis.")
                    .code("COLORVIP")
                    .discountPercent(java.math.BigDecimal.valueOf(20))
                    .active(true)
                    .build(),
                PromotionResponse.builder()
                    .id(2L)
                    .title("Manicura Spa + Pedicura Glam")
                    .description("Combo especial con 15% de descuento los días martes y miércoles.")
                    .code("SPAGLAM")
                    .discountPercent(java.math.BigDecimal.valueOf(15))
                    .active(true)
                    .build()
            );
        }
        return promotions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .code(request.getCode())
            .discountPercent(request.getDiscountPercent())
            .discountAmount(request.getDiscountAmount())
            .bannerUrl(request.getBannerUrl())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .active(request.getActive() != null ? request.getActive() : true)
            .build();

        promotion = promotionRepository.save(promotion);
        return mapToResponse(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        Promotion p = promotionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada"));
        promotionRepository.delete(p);
    }

    private PromotionResponse mapToResponse(Promotion p) {
        return PromotionResponse.builder()
            .id(p.getId())
            .title(p.getTitle())
            .description(p.getDescription())
            .code(p.getCode())
            .discountPercent(p.getDiscountPercent())
            .discountAmount(p.getDiscountAmount())
            .bannerUrl(p.getBannerUrl())
            .startDate(p.getStartDate())
            .endDate(p.getEndDate())
            .active(p.getActive())
            .build();
    }
}
