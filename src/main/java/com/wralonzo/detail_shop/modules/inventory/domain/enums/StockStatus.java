package com.wralonzo.detail_shop.modules.inventory.domain.enums;

public enum StockStatus {
    OK("En existencia", "success"),
    BAJO_STOCK("Bajo stock", "warning"),
    SIN_STOCK("Sin existencias", "danger"),
    NO_INICIALIZADO("No inicializado", "secondary");

    private final String label;
    private final String colorCode;

    StockStatus(String label, String colorCode) {
        this.label = label;
        this.colorCode = colorCode;
    }
}