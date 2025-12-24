package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.projections.PositionTypeProjection;
import com.wralonzo.detail_shop.application.services.PosiontTypeService;
import com.wralonzo.detail_shop.domain.entities.PositionType;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/position-type")
@AllArgsConstructor
public class PositionTypeController {
    private final PosiontTypeService posiontTypeService;

    @GetMapping()
    public ResponseEntity<List<PositionTypeProjection>> getAll(){
        List<PositionTypeProjection> positionTypes = this.posiontTypeService.getAll();
        return ResponseEntity.ok(positionTypes);
    }
}
