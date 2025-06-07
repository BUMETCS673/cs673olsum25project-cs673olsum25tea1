FROM node:20

WORKDIR /app

# Libs needed to run cypress in devcontainer
RUN apt-get update && apt-get install -y \
    libnss3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libgtk-3-0 \
    libx11-xcb1 \
    libxss1 \
    libasound2 \
    libatspi2.0-0 \
    libwayland-client0 \
    libwayland-cursor0 \
    libwayland-egl1 \
    && rm -rf /var/lib/apt/lists/*

