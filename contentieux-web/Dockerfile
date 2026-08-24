FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
# --legacy-peer-deps : @analogjs/vite-plugin-angular ne déclare pas encore de compatibilité
# avec @angular/build 22.x, alors que le projet utilise Angular 22.
RUN --mount=type=cache,target=/root/.npm npm install --legacy-peer-deps
COPY angular.json tsconfig.json tsconfig.app.json ./
COPY src ./src
COPY public ./public
RUN npm run build -- --configuration production

FROM nginx:1.27-alpine
COPY --from=builder /app/dist/contentieux-web/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
# 127.0.0.1 plutôt que localhost : sur cette image, la résolution DNS de "localhost" préfère
# ::1 (IPv6) alors que nginx n'écoute qu'en IPv4, ce qui faisait échouer le contrôle de santé.
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD wget -q -O /dev/null http://127.0.0.1:80/ || exit 1
