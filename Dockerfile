
FROM tomcat:11.0.15-jre21-temurin
LABEL maintainer="wralonzo@gmail.com"
LABEL description="API tet"
COPY target/detail-shop-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/app.war
WORKDIR /usr/local/tomcat

# -- 4. Expone el puerto por defecto de Tomcat (8080)
EXPOSE 8080