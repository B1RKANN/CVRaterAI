# CVRaterAI Web Uygulaması Kurulumu

Bu README, CVRaterAI web uygulamasının VPS'de kurulumu için gerekli adımları içerir.

## Gereksinimler

- Docker ve Docker Compose kurulu bir VPS
- İnternet erişimi

## Kurulum Adımları

1. Bu repo'yu VPS'e klonlayın veya dosyaları VPS'e kopyalayın:

```bash
git clone <repo-url> /opt/cvraterai
cd /opt/cvraterai
```

2. Docker Compose ile uygulamayı başlatın:

```bash
docker-compose up -d
```

3. Uygulamaya erişim:
   - Web arayüzü: `http://<vps-ip>/`
   - API: `http://<vps-ip>/auth/`

## Özelleştirme

### Subdomain veya Domain Kullanımı

`nginx.conf` dosyasındaki `server_name` değerini domain adınızla değiştirin:

```
server_name your-domain.com www.your-domain.com;
```

### SSL Sertifikası Ekleme

1. Let's Encrypt sertifikası edinin:

```bash
apt-get update
apt-get install certbot
certbot certonly --standalone -d your-domain.com -d www.your-domain.com
```

2. `nginx.conf` dosyasını SSL için güncelleyin:

```
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name your-domain.com www.your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # Diğer ayarlar...
}
```

3. `docker-compose.yml` dosyasındaki frontend servisine SSL sertifika yollarını ekleyin:

```yaml
frontend:
  # ...diğer ayarlar
  volumes:
    - ./Web:/usr/share/nginx/html
    - ./nginx.conf:/etc/nginx/conf.d/default.conf
    - /etc/letsencrypt:/etc/letsencrypt:ro
  ports:
    - "80:80"
    - "443:443"
```

## API İsteklerini Ayarlama

VPS'e taşımanın ardından, frontend kodunda API endpoint'leri güncellemeniz gerekebilir. Bunun için `Web/script.js` dosyasındaki API URL'lerini VPS IP adresinizle güncelleyin:

```javascript
// Örnek: http://69.62.120.202:8080/auth/ yerine
// Yeni: http://your-domain.com/auth/ veya http://<vps-ip>/auth/
```

## Sorun Giderme

### Log Kontrolü

```bash
# Backend logları
docker logs backend

# Frontend (Nginx) logları
docker logs frontend

# Database logları
docker logs db
```

### Servis Yeniden Başlatma

```bash
docker-compose restart frontend
docker-compose restart backend
```

### Tüm Servisleri Yeniden Başlatma

```bash
docker-compose down
docker-compose up -d
``` 