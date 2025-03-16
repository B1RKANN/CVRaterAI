#!/bin/bash

# Root yetkisi kontrolü
if [ "$(id -u)" != "0" ]; then
   echo "Bu script'in root yetkisiyle çalıştırılması gerekiyor" 
   exit 1
fi

echo "CVRaterAI - VPS Kurulum Script'i"
echo "=================================="

# Sistem güncellemesi
echo "Sistem güncellemesi yapılıyor..."
apt-get update
apt-get upgrade -y

# Docker kurulumu
echo "Docker kurulumu yapılıyor..."
apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io

# Docker-Compose kurulumu
echo "Docker Compose kurulumu yapılıyor..."
curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Firewall yapılandırması
echo "Firewall yapılandırılıyor..."
apt-get install -y ufw
ufw allow ssh
ufw allow 8080/tcp
ufw --force enable

# Dosya yapısı oluşturma
echo "Dosya yapısı oluşturuluyor..."
mkdir -p /opt/cvraterai/uploads

# Docker imajı yükleme (eğer dosya varsa)
if [ -f "cvraterai-backend.tar" ]; then
    echo "Docker imajı yükleniyor..."
    docker load < cvraterai-backend.tar
else
    echo "Docker imajı bulunamadı. Lütfen docker save ile bir imaj oluşturun veya Dockerfile ile build alın."
fi

# Docker Compose dosyasını kopyalama
if [ -f "docker-compose.yml" ]; then
    echo "Docker Compose dosyası kopyalanıyor..."
    cp docker-compose.yml /opt/cvraterai/
    cd /opt/cvraterai/
    
    # Docker Compose ile uygulamayı başlatma
    echo "Uygulama başlatılıyor..."
    docker-compose up -d
    
    echo "Kurulum tamamlandı! Uygulamanız http://sunucu-ip:8080 adresinde çalışıyor olmalı."
else
    echo "docker-compose.yml dosyası bulunamadı."
    exit 1
fi 