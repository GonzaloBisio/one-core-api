# Etapa 1: Construcción con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

# Establece el directorio de trabajo para la construcción
WORKDIR /build-src

# Copia solo el pom.xml primero para cachear las dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente
COPY src ./src

# Empaqueta la aplicación (asegúrate que esto genere el JAR en /build-src/target/)
# -DskipTests es para acelerar el build en el Dockerfile; las pruebas se deben correr antes o en un pipeline de CI.
RUN mvn package -DskipTests


# Etapa 2: Creación de la imagen final
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copia el JAR desde la etapa de construcción 'builder'
COPY --from=builder /build-src/target/one-core-0.0.1-SNAPSHOT.jar app.jar
# Si tu nombre de JAR es diferente, ajústalo arriba.
# Puedes verificar el nombre exacto en la salida de 'mvn package' o listando /build-src/target/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]