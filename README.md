# Contents
- [GetActive](#SoloSavings)
- [Software Architecture](#software-architecture)
- [Class Diagram](#class-diagram)
- [Database Diagram](#database-diagram)
- [Docker Environment Configuration](#docker-environment-configuration)
- [Environment Configuration](#environment-configuration)
- [Development Guide](#development-guide)
- [Troubleshooting](#troubleshooting)
- [Important Notes](#important-notes)
- [Contribution Guidelines](#contribution-guidelines)

[![CD workflow](https://github.com/BUMETCS673/CS673OLSum25Team1/actions/workflows/cd.yml/badge.svg)](https://github.com/BUMETCS673/CS673OLSum25Team1/actions/workflows/cd.yml) 
[![CI workflow](https://github.com/BUMETCS673/CS673OLSum25Team1/actions/workflows/ci.yml/badge.svg)](https://github.com/BUMETCS673/CS673OLSum25Team1/actions/workflows/ci.yml)

![Coverage](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/BUMETCS673/CS673OLSum25Team1/coverage-badge/code/backend/badges/coverage.json)


# GetActive
<img src="misc/logo.png" alt="drawing" width="100"/>

GetActive helps university students overcome the challenge of finding and joining social or recreational groups. It offers a platform to discover, create, and join on-campus activities, making it easier to build connections. Students can register, verify their accounts, and manage their participation in multiple activities. The goal is to promote an active, socially connected campus experience.

# Software Architecture  

![alt text](misc/ApplicationComponentDiagram.svg)

The GetActive application is composed of modular components distributed across a client-server architecture. 
The system is decomposed into three main components: the frontend, the backend, and the database, each of which is 
containerized using Docker and deployed to AWS cloud platform. The architecture follows a layered design pattern within 
the backend and uses industry-standard frameworks and tools for development, deployment, and scalability.

1. **Database Service (db)**
- Uses MySQL 8.0
- Port mapping: 3306:3306
- Data persistence: Uses Docker volume (mysql_data)
- Health check: Checks database connection status every 10 seconds

2. **Backend Service (backend)**
- Based on Spring Boot 3.x
- Port mapping: 3232:3232
- Dependencies: Requires database service to be healthy
- Health check: Checks API health status every 10 seconds

3. **Frontend Service (frontend)**
- Based on Node.js and React
- Port mapping: 80:80
- Uses Nginx as web server
- Dependencies: Requires backend service to be healthy

### Technology Stack and Frameworks
* Frontend: React, HTML5, CSS3, Axios/Fetch API (for REST communication)
* Backend: Java, Spring Boot, Spring Security, Spring Data JPA
* Database: MySQL
* Authentication: JWT (JSON Web Tokens)
* Deployment: Docker, GitHub, AWS, GitHub Actions
* Version Control: Git, GitHub


### Package Structure
![alt text](misc/PackageDiagram.svg)

## Class Diagram
![alt text](misc/ClassDiagram.svg)

## Database Diagram
![alt text](misc/DatabaseDiagram.svg)


# Docker Environment Configuration
This [code](code) folder contains all source code and test code.
This document provides detailed instructions for the Docker environment configuration and usage of the GetActive project.
The project uses Docker Compose to manage multiple services, including frontend, backend, and database services.


## Quick Start

### Prerequisites

- Install [Docker](https://docs.docker.com/get-docker/)
- Install [Docker Compose](https://docs.docker.com/compose/install/)

### Starting Services

1. After cloning the project, navigate to the project root directory:
   ```bash
   cd code
   ```

2. Start all services using Docker Compose:
   ```bash
   docker-compose --profile prod up -d
   ```

3. Check service status:
   ```bash
   docker-compose ps
   ```

### Accessing Services

- Frontend application: http://localhost
- Backend API: http://localhost:3232/v1
- Database: localhost:3306

## Environment Configuration

### Database Configuration
- Database name: getactive
- Username: root
- Password: password
- Port: 3306

### Backend Configuration
- Service port: 3232
- Health check endpoint: /v1/health
- Database connection configuration in application.properties

### Frontend Configuration
- Web server port: 80
- API proxy configuration in nginx.conf

## Development Guide

### Rebuilding Services

When code changes are made, services need to be rebuilt:

```bash
# Rebuild all services
docker-compose build

# Rebuild specific service
docker-compose build [service_name]  # Example: docker-compose build backend
```

### Viewing Logs

```bash
# View logs for all services
docker-compose logs

# View logs for specific service
docker-compose logs [service_name]  # Example: docker-compose logs backend

# View logs in real-time
docker-compose logs -f
```

### Stopping Services

```bash
# Stop all services
docker-compose down

# Stop services and remove volumes
docker-compose down -v
```

## Troubleshooting

1. **Port Conflicts**
  - Ensure ports 80, 3232, and 3306 are not in use by other services
  - Use `lsof -i :[port]` to check port usage

2. **Service Startup Issues**
  - Check Docker logs: `docker-compose logs [service_name]`
  - Ensure all dependent services are running properly
  - Verify environment variables and configuration files

3. **Database Connection Issues**
  - Ensure database container is running
  - Check database connection configuration
  - Verify database user permissions

4. **Frontend Access Issues**
  - Check Nginx configuration
  - Ensure backend API is accessible
  - Check browser console for error messages

## Important Notes

1. This configuration is for development environment only, not recommended for production use
2. Database data is persisted through Docker volume, data won't be lost when containers are removed
3. Services need to be rebuilt after code changes
4. Ensure Docker and Docker Compose versions are compatible

## Contribution Guidelines

1. Test Docker configuration changes locally before committing
2. Update this documentation when configuration changes are made
3. Ensure all services can start and run properly before submitting code


