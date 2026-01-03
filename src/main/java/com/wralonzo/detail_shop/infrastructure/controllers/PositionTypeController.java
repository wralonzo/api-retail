package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.application.services.PosiontTypeService;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/position-type")
@AllArgsConstructor
public class PositionTypeController {
    private final PosiontTypeService posiontTypeService;

    @GetMapping()
    public ResponseEntity<Page<PositionTypeProjection>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<PositionTypeProjection> positionTypes = this.posiontTypeService.getAll(pageable);
        return ResponseEntity.ok(positionTypes);
    }
}
