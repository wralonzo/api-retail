package com.wralonzo.detail_shop.modules.auth.infraestructure;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.auth.application.PositionTypeService;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.positiontype.PositionTypeRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.PositionTypeProjection;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/position-type")
@RequiredArgsConstructor
public class PositionTypeController {
    private final PositionTypeService positionTypeService;

    @GetMapping()
    public ResponseEntity<Page<PositionTypeProjection>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<PositionTypeProjection> positionTypes = this.positionTypeService.getAll(pageable);
        return ResponseUtil.ok(positionTypes);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PositionType> create(@Valid @RequestBody PositionTypeRequest request) {
        PositionType positionType = this.positionTypeService.create(request);
        return ResponseUtil.ok(positionType);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionType> getById(@PathVariable Long id) {
        PositionType positionType = this.positionTypeService.findById(id);
        return ResponseUtil.ok(positionType);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PositionType> update(@PathVariable Long id, @Valid @RequestBody PositionTypeRequest request) {
        PositionType positionType = this.positionTypeService.update(id, request);
        return ResponseUtil.ok(positionType);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.positionTypeService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Tipo de posición eliminado exitosamente"));
    }
}
