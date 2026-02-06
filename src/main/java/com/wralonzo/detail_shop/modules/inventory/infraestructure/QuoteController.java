package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.application.quote.QuoteService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote.QuoteRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.quote.QuoteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    public ResponseEntity<Page<QuoteResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(quoteService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.getById(id));
    }

    @PostMapping
    public ResponseEntity<QuoteResponse> create(@Valid @RequestBody QuoteRequest request) {
        return new ResponseEntity<>(quoteService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponse> update(@PathVariable Long id, @Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(quoteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
