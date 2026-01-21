package com.wralonzo.detail_shop.modules.inventory.domain.enums;

public enum MovementType {
  ENTRADA_COMPRA, // Ingreso de mercancía de proveedor
  SALIDA_VENTA, // Venta a cliente
  TRANSFERENCIA_ENTRADA, // Recepción de otra bodega
  TRANSFERENCIA_SALIDA, // Envío a otra bodega
  AJUSTE_INVENTARIO, // Corrección manual (merma, pérdida, etc.)
  DEVOLUCION_CLIENTE,
  DEVOLUCION_PROVEEDOR
}