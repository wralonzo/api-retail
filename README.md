# Detail Shop API

Sistema de gestión backend para retail ("Detail Shop"), construido con Spring Boot. Este sistema administra el inventario, ventas, reservaciones y estructura organizacional para un negocio de venta al detalle.

## 🚀 Tecnologías

*   **Java 17+**
*   **Spring Boot 3.x**
*   **Spring Data JPA / Hibernate**
*   **PostgreSQL** (Base de datos recomendada)
*   **Docker & Docker Compose**
*   **Maven**

## 📦 Módulos Principales

### 1. Organización (`/organization`)
Gestiona la estructura jerárquica del negocio:
*   **Company**: Entidad raíz.
*   **Branch**: Sucursales asociadas a una compañía.
*   **Warehouse**: Bodegas físicas para almacenamiento de inventario.

### 2. Inventario (`/inventory`)
Núcleo del sistema de productos y stock:
*   **Product**: Soporte para productos estándar, servicios y **combos (bundles)**.
*   **Units**: Manejo de unidades de medida flexibles (ej: Unidad, Caja, Paquete) con factores de conversión.
*   **ProductBranchConfig**: Configuración de precios y parámetros por sucursal.
*   **Inventory / InventoryBatch**: Control de existencias, incluyendo manejo de lotes y fechas de vencimiento (FEFO).
*   **InventoryMovement**: Registro de auditoría de todos los movimientos de entrada y salida.

### 3. Ventas (`/sales`)
Procesamiento de transacciones comerciales:
*   Generación de ventas vinculadas a clientes e inventario.
*   Validación automática de stock.
*   Descuento de inventario en tiempo real.

### 4. Reservaciones (`/reservations`)
Sistema de apartado de mercancía:
*   Permite reservar stock por un tiempo determinado.
*   Conversión de reservaciones a ventas confirmadas.
*   Cancelación y liberación de stock.
*   Validaciones de crédito corporativo (en progreso).

### 5. Clientes (`/customers`)
Gestión de la base de datos de clientes, perfiles y límites de crédito.

## 🛠️ Configuración y Ejecución

### Requisitos Previos
*   JDK 17 o superior.
*   Docker (opcional, para base de datos).

### Base de Datos
El proyecto incluye un `compose.yaml` para levantar la infraestructura necesaria:
```bash
docker-compose up -d
```

### Ejecutar la Aplicación
Usando Maven wrapper:

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

O generar el artefacto:
```bash
./mvnw clean package -DskipTests
java -jar target/detail-shop-0.0.1-SNAPSHOT.jar
```

## 📝 Notas de Desarrollo
*   **Architecture**: Arquitectura hexagonal/modular simplificada.
*   **Auth**: Integración con servicios de autenticación (ej: Google Auth).

---
*Generado por Antigravity*
