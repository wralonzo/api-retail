# --- Etapa 1: Compilación ---
# Usar una imagen con el JDK y Maven preinstalados
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo pom.xml y las dependencias para una mejor caché
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del código fuente
COPY src ./src

# Compilar la aplicación y generar el JAR
RUN mvn clean package -DskipTests

# --- Etapa 2: Creación de la imagen final ---
# Usar una imagen base ligera con solo el JRE
FROM openjdk:21-slim

# Exponer el puerto de la aplicación
EXPOSE 8080

# Copiar el archivo JAR desde la etapa de compilación
# El nombre del archivo JAR se asume que es 'tu-aplicacion.jar'
COPY --from=build /app/target/detail-shop.jar app.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]

#docker build -t wralonzo/retailapi:1.0 .