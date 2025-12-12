# =================================================================
# SCRIPT DE DESPLIEGUE AUTOMÁTICO - WINDOWS (PowerShell)
# =================================================================

# Nombre del contenedor y de la imagen
$CONTAINER_NAME = "detail-shop-container"
$IMAGE_NAME = "detail-shop-api:latest"

Write-Host "--- 1. Limpiando y Empaquetando la aplicación con Maven ---"
mvn clean package -DskipTests

# Verificar si el empaquetado fue exitoso
if ($LASTEXITCODE -ne 0) {
    Write-Error "ERROR: Falló el empaquetado de Maven."
    exit 1
}

Write-Host "--- 2. Deteniendo el contenedor antiguo ($CONTAINER_NAME) ---"
# El '-f' ignora el error si el contenedor no existe
docker stop $CONTAINER_NAME 2>&1 | Out-Null

Write-Host "--- 3. Eliminando el contenedor antiguo ($CONTAINER_NAME) ---"
# El '-f' ignora el error si el contenedor no existe
docker rm $CONTAINER_NAME 2>&1 | Out-Null

Write-Host "--- 4. Construyendo la nueva imagen de Docker ($IMAGE_NAME) ---"
docker build -t $IMAGE_NAME .

# Verificar si la construcción de Docker fue exitosa
if ($LASTEXITCODE -ne 0) {
    Write-Error "ERROR: Falló la construcción de la imagen de Docker."
    exit 1
}

Write-Host "--- 5. Ejecutando el nuevo contenedor en el puerto 8080 ---"

docker compose up -d --build

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ ¡Despliegue completado con éxito! La aplicación está en http://localhost:8080/app/api" -ForegroundColor Green
} else {
    Write-Error "ERROR: Falló al iniciar el nuevo contenedor."
    exit 1
}