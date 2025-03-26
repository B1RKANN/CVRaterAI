FROM node:18-slim AS base

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app

# Copy package.json and package-lock.json
COPY package.json package-lock.json ./
# npm ci yerine npm install kullanarak package-lock.json'ı güncelliyoruz
RUN npm install

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Next.js telemetry devre dışı
ENV NEXT_TELEMETRY_DISABLED 1

# İzin sorunlarını çözmek için node_modules/.bin izinlerini düzeltiyoruz
RUN chmod +x ./node_modules/.bin/next
# Cache kullanımını optimize etmek için
RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

# Güvenlik için non-root kullanıcı
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Sadece gerekli dosyaları kopyalıyoruz
COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

# Non-root kullanıcıya geçiş
USER nextjs

EXPOSE 3000

ENV PORT 3000

# Healthcheck ekleyerek container durumunu kontrol ediyoruz
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

CMD ["node", "server.js"] 