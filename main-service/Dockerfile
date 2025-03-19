# Используем OpenJDK 21
FROM openjdk:21-jdk

# Рабочая директория
WORKDIR /app

# Копируем JAR-файл
COPY target/main-service.jar main-service.jar

# Запуск
CMD ["java", "-jar", "main-service.jar"]