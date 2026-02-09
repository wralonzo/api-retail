package com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLoadResponse {

    private Boolean success;

    private String message;

    private Integer itemsProcessed;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
