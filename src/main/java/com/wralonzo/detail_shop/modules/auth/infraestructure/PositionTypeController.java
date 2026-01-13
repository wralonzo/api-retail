package com.wralonzo.detail_shop.modules.auth.infraestructure;

import com.wralonzo.detail_shop.modules.auth.application.PosiontTypeService;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.PositionTypeProjection;

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
