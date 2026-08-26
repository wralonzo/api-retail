package com.wralonzo.detail_shop.modules.promotions.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "El título de la promoción es obligatorio")
    private String title;

    private String description;

    private String code;

    private BigDecimal discountPercent;

    private BigDecimal discountAmount;

    private String bannerUrl;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;
}
