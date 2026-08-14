FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY angular.json tsconfig.json tsconfig.app.json ./
COPY src ./src
RUN npm run build -- --configuration production

FROM nginx:1.27-alpine
COPY --from=builder /app/dist/contentieux-web/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD wget -q -O /dev/null http://localhost:80/ || exit 1
