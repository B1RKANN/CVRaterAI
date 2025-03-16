# CVRaterAI - VPS Kurulum Rehberi

Bu dokümanda, CVRaterAI uygulamasını bir VPS (Sanal Özel Sunucu) üzerinde nasıl kuracağınız anlatılmaktadır.

## Ön Gereksinimler

- Ubuntu 20.04 LTS veya üzeri bir VPS
- Root erişimi
- En az 2GB RAM, 2 CPU çekirdek ve 20GB disk alanı

## Kurulum Adımları

### 1. Dosyaları VPS'ye Yükleme

Öncelikle hazırladığınız dosyaları VPS'ye aktarın:

```bash
# Yerel bilgisayarınızda dosyaları sıkıştırın
zip -r cvraterai-deploy.zip docker-compose.yml vps-setup.sh cvraterai-backend.tar

# SCP ile VPS'ye aktarın
scp cvraterai-deploy.zip kullanici@vps-ip:/tmp/

# VPS'de SSH ile bağlanın
ssh kullanici@vps-ip

# Dosyaları çıkarın
cd /tmp
unzip cvraterai-deploy.zip
```

### 2. Kurulum Script'ini Çalıştırma

Kurulum script'ini çalıştırarak otomatik kurulumu başlatın:

```bash
# Script'e çalıştırma yetkisi verin
chmod +x vps-setup.sh

# Root yetkisiyle çalıştırın
sudo ./vps-setup.sh
```

Bu script otomatik olarak şunları yapacaktır:
- Sistem güncellemelerini yükler
- Docker ve Docker Compose'u kurar
- Güvenlik duvarını yapılandırır
- Docker imajını yükler
- Uygulamayı başlatır

### 3. Kurulumu Doğrulama

Kurulum tamamlandıktan sonra, uygulamanızın düzgün çalıştığını doğrulayın:

```bash
# Docker container'larının çalıştığını kontrol edin
docker ps

# Logları kontrol edin
docker logs backend
```

Tarayıcınızda `http://vps-ip:8080` adresine giderek uygulamanın çalıştığını doğrulayabilirsiniz.

## Sorun Giderme

### Veritabanı Bağlantı Sorunları

Eğer uygulamanız veritabanına bağlanamazsa:

```bash
# PostgreSQL container'ının çalıştığını kontrol edin
docker ps | grep postgres

# PostgreSQL loglarını kontrol edin
docker logs db
```

### Uygulama Hataları

Uygulama loglarını kontrol edin:

```bash
docker logs backend
```

## Bakım

### Uygulamayı Güncelleme

Yeni bir Docker imajıyla uygulamayı güncellemek için:

```bash
# Yeni imajı VPS'ye aktarın
scp yeni-imaj.tar kullanici@vps-ip:/tmp/

# VPS'de imajı yükleyin
docker load < /tmp/yeni-imaj.tar

# Docker Compose ile yeniden başlatın
cd /opt/cvraterai
docker-compose up -d --force-recreate backend
```

### Veritabanı Yedekleme

PostgreSQL veritabanını yedeklemek için:

```bash
# Veritabanını dışa aktarın
docker exec -t db pg_dump -U cvrateruser cvraterai > backup.sql

# Yedeği indirin
scp kullanici@vps-ip:backup.sql .
``` 