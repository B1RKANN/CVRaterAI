# CVRaterAI Backend için Dockerfile
FROM openjdk:17-jdk-slim

# Çalışma dizinini belirle
WORKDIR /app

# Maven bağımlılıklarını ekle
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Uygulamayı kopyala
COPY src src

# Projeyi derle
RUN ./mvnw clean package -DskipTests

# Derleme sonucunda oluşan JAR dosyasını belirlenen konuma taşı
RUN mkdir -p target && cp target/*.jar app.jar

# Yüklenen dosyalar için bir volüm oluştur
VOLUME /app/uploads

# Uygulamanın dinleyeceği portu belirt
EXPOSE 8080

# Ortam değişkenlerini ayarla
ENV SPRING_PROFILES_ACTIVE=prod
ENV GEMINI_API_KEY=AIzaSyDyUAJK7t3bJ72YbLrHjrCgyD1v99ijTZQ
ENV JWT_SECRET=7UiEQwqHpPeuR9HEyd2NcEgluhuJ9Ctdn8/8lMyvUrY=
ENV FILE_UPLOAD_DIR=/app/uploads

# Uygulamayı başlat
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"] 