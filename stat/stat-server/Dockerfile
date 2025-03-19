# Используем OpenJDK 21
FROM openjdk:21-jdk

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем JAR-файл (после сборки Maven)
COPY target/stat-server.jar stat-server.jar

# Команда для запуска
CMD ["java", "-jar", "stat-server.jar"]