# La etapa de construcción mantiene Maven y las herramientas fuera de la imagen final.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Separar el POM permite reutilizar la caché de dependencias entre cambios de código.
COPY pom.xml .
RUN mvn -B dependency:go-offline
# El código se copia después de resolver dependencias para aprovechar la caché de Docker.
COPY src ./src
RUN mvn -B -DskipTests package

# La imagen de ejecución solo necesita un JRE 21, reduciendo su tamaño.
FROM eclipse-temurin:21-jre
WORKDIR /app
# El usuario dedicado limita los permisos del proceso de la aplicación.
RUN groupadd --system spring && useradd --system --gid spring --home-dir /app spring
# Copiar por patrón evita acoplar la imagen al nombre o versión del artefacto.
COPY --from=build /app/target/*.jar app.jar
USER spring
# Ajusta el uso de memoria de la JVM al límite de memoria del contenedor.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
