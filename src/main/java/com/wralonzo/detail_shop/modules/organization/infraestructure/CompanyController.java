package com.wralonzo.detail_shop.modules.organization.infraestructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.organization.application.CompanyService;
import com.wralonzo.detail_shop.modules.organization.domain.dtos.company.CompanyRequest;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Company;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping()
    public ResponseEntity<Page<Company>> getAll(
            @PageableDefault(size = 10, sort = "businessName") Pageable pageable) {
        Page<Company> companies = this.companyService.getAll(pageable);
        return ResponseUtil.ok(companies);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Company> create(@Valid @RequestBody CompanyRequest request) {
        Company company = this.companyService.create(request);
        return ResponseUtil.ok(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getById(@PathVariable Long id) {
        Company company = this.companyService.getById(id);
        return ResponseUtil.ok(company);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Company> update(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        Company company = this.companyService.update(id, request);
        return ResponseUtil.ok(company);
    }

    @PatchMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        this.companyService.delete(id);
        return ResponseUtil.ok(Map.of("message", "Compañía eliminada exitosamente"));
    }
}
