package com.wralonzo.detail_shop.modules.organization.infraestructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.organization.application.BranchService;
import com.wralonzo.detail_shop.modules.organization.domain.dtos.branch.BranchRequest;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping()
    public ResponseEntity<Page<Branch>> getAll(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<Branch> branches = this.branchService.getAll(pageable);
        return ResponseUtil.ok(branches);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Branch> create(@Valid @RequestBody BranchRequest request) {
        Branch branch = this.branchService.create(request);
        return ResponseUtil.ok(branch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getById(@PathVariable Long id) {
        Branch branch = this.branchService.findById(id);
        return ResponseUtil.ok(branch);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Branch> update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        Branch branch = this.branchService.update(id, request);
        return ResponseUtil.ok(branch);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.branchService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Sucursal eliminada exitosamente"));
    }
}
