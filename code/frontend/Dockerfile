FROM node:18-alpine AS build-stage

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Output stage - this stage is just for copying the build output
FROM alpine:latest

# Create a directory for the build output
WORKDIR /build-output

# Copy only the built files from the builder stage
COPY --from=build-stage /app/dist ./dist

FROM nginx:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build-stage /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]