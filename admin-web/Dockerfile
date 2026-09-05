# syntax=docker/dockerfile:1
FROM node:22-alpine AS build
WORKDIR /app

COPY admin-web/package*.json ./
COPY admin-web/.npmrc ./
RUN npm ci

COPY admin-web/ ./
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
