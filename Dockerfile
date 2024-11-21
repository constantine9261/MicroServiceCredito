# Usar una imagen base ligera de Java
FROM openjdk:17-jdk-slim

# Crear un directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo JAR generado al contenedor
COPY target/microserviceCredit-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto que utiliza el microservicio
EXPOSE 8080

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
