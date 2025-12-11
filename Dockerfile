# -- Usa la imagen base oficial de Tomcat (Versión 10 para Jakarta EE 9/Spring Boot 3+ o la que necesites)
FROM tomcat:10.1-jdk17-temurin

# -- Metadatos del Contenedor (Opcional)
LABEL maintainer="wralonzo@gmail.com"
LABEL description="API de Detail Shop desplegada en Tomcat"

# -- 1. Elimina el archivo ROOT.war por defecto (opcional)
# RUN rm -rf /usr/local/tomcat/webapps/ROOT/

# -- 2. Copia tu archivo WAR compilado AL MISMO TIEMPO QUE LO RENOMBRAS a ROOT.war
# 🛑 CAMBIO CLAVE AQUÍ: Se especifica el nombre de destino 'ROOT.war'
COPY target/detail-shop-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/app.war


# -- 3. Configura el directorio de trabajo (Opcional, para facilitar la administración)
WORKDIR /usr/local/tomcat

# -- 4. Expone el puerto por defecto de Tomcat (8080)
EXPOSE 8080

CMD ["catalina.sh", "run"]