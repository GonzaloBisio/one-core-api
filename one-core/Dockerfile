# =================================================================
# ETAPA 1: Construcción (Build Stage)
# Usamos una imagen de Maven con JDK 21 para compilar el proyecto.
# =================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# --- CORRECCIÓN AQUÍ ---
# Copiamos los archivos desde la raíz del contexto de build, que ya es la carpeta "one-core"
COPY pom.xml .
COPY src ./src

# Ejecutamos el comando de Maven para construir el proyecto y generar el JAR.
# -DskipTests omite la ejecución de tests para un build más rápido.
RUN mvn -B package -DskipTests


# =================================================================
# ETAPA 2: Ejecución (Runtime Stage)
# Usamos una imagen base de Java mucho más ligera (slim) para la ejecución,
# lo que resulta en una imagen final más pequeña y segura.
# =================================================================
FROM openjdk:21-slim

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos ÚNICAMENTE el archivo .jar desde la etapa de construcción anterior.
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto en el que corre la aplicación Spring Boot
EXPOSE 8080

# El comando que se ejecutará cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]