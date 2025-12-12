#!/bin/bash
# =================================================================
# SCRIPT DE DESPLIEGUE AUTOMÁTICO - LINUX / MACOS (Bash)
# =================================================================

CONTAINER_NAME="detail-shop-container"
IMAGE_NAME="detail-shop-api:latest"

echo "--- 1. Limpiando y Empaquetando la aplicación con Maven ---"
# El argumento -DskipTests es clave para acelerar el proceso
mvn clean package -DskipTests

# Verificar si el empaquetado fue exitoso
if [ $? -ne 0 ]; then
    echo "ERROR: Falló el empaquetado de Maven."
    exit 1
fi

echo "--- 2. Deteniendo y Eliminando el contenedor antiguo ($CONTAINER_NAME) ---"
# El || true permite que el script continúe si la parada/eliminación falla (ej. no existe)
docker stop "$CONTAINER_NAME" || true
docker rm "$CONTAINER_NAME" || true

echo "--- 3. Construyendo la nueva imagen de Docker ($IMAGE_NAME) ---"
docker build -t "$IMAGE_NAME" .

# Verificar si la construcción de Docker fue exitosa
if [ $? -ne 0 ]; then
    echo "ERROR: Falló la construcción de la imagen de Docker."
    exit 1
fi

echo "--- 4. Ejecutando el nuevo contenedor en el puerto 8080 ---"
docker run -d -p 8080:8080 --name "$CONTAINER_NAME" "$IMAGE_NAME"

if [ $? -eq 0 ]; then
    echo "✅ ¡Despliegue completado con éxito! La aplicación está en http://localhost:8080/app/api/auth/login"
else
    echo "ERROR: Falló al iniciar el nuevo contenedor."
    exit 1
fi