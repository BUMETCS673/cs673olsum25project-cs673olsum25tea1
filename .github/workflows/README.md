# Continuous Integration (CI) Workflow

This document describes the automated CI workflow for the GetActive project.

## Overview

Our CI pipeline runs automatically on every push and pull request to any branch. It performs integrated testing across both backend and frontend components to ensure code quality and functionality.

## Workflow Steps

1. **Environment Setup**
   - Runs on Ubuntu latest
   - Spins up a MySQL 8.0 database service for testing

2. **Database Initialization**
   - Initializes the MySQL database with required schema and test data

3. **Backend Testing & Deployment**
   - Sets up JDK 17 and Gradle
   - Runs backend unit tests with connection to the test database
   - Builds the backend application (excluding tests)
   - Starts the backend service and verifies health check endpoint

4. **Frontend Testing**
   - Sets up Node.js 18 with npm caching
   - Installs frontend dependencies
   - Runs frontend unit tests

## Running Locally

To run the CI workflow locally before pushing:

1. After cloning the repository, please run the following command to enable the project's Git hooks:`chmod +x .githooks/pre-commit && git config core.hooksPath .githooks`
2. Start a MySQL database: `cd ./code/database && docker build -t getactive-db . && docker run -d --name getactive-mysql -p 3306:3306 getactive-db`
3. Database management tool: MySQLWorkbench. username: root, password: password
4. Run backend tests: `./gradlew test`
5. Build backend: `./gradlew build -x test`
6. Start backend service: `java -jar build/libs/getactivecore-0.0.1-SNAPSHOT.jar` and verify health check at http://localhost:3232/v1/health
7. Run frontend tests: `npm run test`

## Troubleshooting

If the CI workflow fails:
- Check backend logs for service startup issues
- Verify database connection settings
- Ensure all tests pass locally before pushing

## GitHub Actions Configuration

The detailed configuration can be found in `.github/workflows/ci.yml`.

# Continuous Deployment (CD) Workflow

This document describes the Continuous Deployment (CD) workflow पानी (Pānī) for this project, which automates the building of Docker images and their deployment to an AWS EC2 instance.

## Workflow Overview

The CD workflow is defined in `.github/workflows/cd.yml` and consists of two main jobs:

1.  **`build-and-push`**: This job is triggered when a new tag matching the pattern `v*` (e.g., `v0.1.0`, `v1.2.3`) is pushed to the repository.
    *   Checks out the repository code.
    *   Logs in to GitHub Container Registry (ghcr.io).
    *   Sets up Docker Buildx, using the `docker` driver to ensure local image visibility for multi-stage builds.
    *   Builds Docker images for the database, backend, and frontend (the frontend image is built locally but not pushed, its artifacts are used by the Nginx image).
    *   Builds the Nginx Docker image (which includes frontend artifacts) and pushes it to ghcr.io along with the database and backend images. All pushed images are tagged with the Git tag that triggered the workflow (e.g., `v0.1.0`).
    *   Includes a step to list local Docker images after the frontend build for debugging purposes.

2.  **`deploy`**: This job runs after `build-and-push` completes successfully.
    *   Connects to a pre-configured AWS EC2 instance via SSH.
    *   Navigates to the project directory on the EC2 instance (e.g., `/srv/getactive`).
    *   Logs in to ghcr.io on the EC2 instance.
    *   Pulls the newly built database, backend, and Nginx images from ghcr.io pensamiento (Pensamiento) with the latest Git tag.
    *   Updates the `docker-compose.yml` file on the EC2 instance, modifying the image tags for each service to point to the newly pulled versions.
    *   Restarts the services using `docker-compose down` and `docker-compose up -d`.
    *   Cleans up old, unused Docker images on the EC2 instance to save disk space.

## Prerequisites

Before using this CD workflow, ensure the following are set up:

### 1. AWS EC2 Instance

*   An AWS EC2 instance provisioned and accessible via SSH.
*   **Docker Engine installed:** Follow instructions for your EC2 instance's OS (e.g., Amazon Linux 2, Ubuntu). Ensure the Docker service is running and enabled on boot.
*   **Docker Compose installed:** Docker Compose V1 (`docker-compose`) or V2 (`docker compose`) must be installed. The current `cd.yml` script uses `docker-compose` (V1 syntax).
*   **Deployment User Permissions:** The user specified for SSH connection must be part of the `docker` group to execute Docker commands without `sudo`. (e.g., `sudo usermod -aG docker your_ec2_user` and then the user needs to log out and log back in).
*   **Project Directory:** Create the project directory on the EC2 instance where your `docker-compose.yml` will reside and where the application will run (e.g., `/srv/getactive`). The deployment user must have write permissions to this directory if `docker-compose.yml` is managed by the user, or write permission to the `docker-compose.yml` file itself for `sed` commands.
*   **Firewall/Security Group:** The EC2 instance's security group must allow incoming SSH connections (port 22) from GitHub Actions runners (or a wider range if necessary, though more specific is better for security).

### 2. `docker-compose.yml` on EC2

*   A `docker-compose.yml` (or `docker-compose.prod.yml` - adjust `cd.yml` script accordingly) file must be present in the project directory on the EC2 instance (e.g., `/srv/getactive/docker-compose.yml`).
*   This file should define the `db`, `backend`, and `nginx` services.
*   The `image` fields for these services should point to the correct base ghcr.io paths, for example:
    ```yaml
    services:
      db:
        image: ghcr.io/your-org/your-repo/db:initial_tag
      backend:
        image: ghcr.io/your-org/your-repo/backend:initial_tag
      nginx:
        image: ghcr.io/your-org/your-repo/nginx:initial_tag
    ```
    The `initial_tag` will be replaced by the CD script with the new Git tag.
*   If your services require environment variables (especially secrets like database passwords), manage them securely on the EC2 instance, for example, using a `.env` file in the project directory (ensure `.env` is in your `.gitignore`) or by setting environment variables directly on the host.

### 3. GitHub Secrets

The following encrypted secrets must be configured in your GitHub repository settings (`Settings` > `Secrets and variables` > `Actions`):

*   `EC2_HOST`: The public IP address or DNS name of your AWS EC2 instance.
*   `EC2_USERNAME`: The username for SSHing into your EC2 instance (e.g., `ec2-user`, `ubuntu`).
*   `EC2_SSH_KEY`: The private SSH key (in PEM format, including `-----BEGIN ... KEY-----` and `-----END ... KEY-----` lines) corresponding to a public key authorized on your EC2 instance for the `EC2_USERNAME`.

### 4. Dockerfiles

*   Ensure `Dockerfile`s are present in `./code/database`, `./code/backend`, `./code/frontend`, and `./code/nginx` as referenced by the `build-and-push` job.
*   The Nginx Dockerfile (`./code/nginx/Dockerfile`) should be set up to copy build artifacts from the frontend image (e.g., using a multi-stage build like `COPY --from=frontend /app/dist /usr/share/nginx/html`).

## How to Trigger the Workflow

1.  Commit and push all your code changes to the repository.
2.  Create a new Git tag with the `v*` prefix (e.g., `v0.1.0`, `v0.2.0-alpha`).
    ```bash
    git tag v0.1.0
    ```
3.  Push the tag to the remote repository:
    ```bash
    git push origin v0.1.0
    ```
    Pushing the tag will automatically trigger the `build-and-push` job, followed by the `deploy` job upon its success.

## Monitoring the Workflow

*   You can monitor the progress of the workflow runs in the "Actions" tab of your GitHub repository.
*   Each job and step will output logs, which are useful for diagnosing any issues.

## Troubleshooting

*   **SSH Connection Issues (`ssh: no key found` or connection timeouts):**
    *   Verify `EC2_SSH_KEY` secret: Ensure it's the complete and correct private key, in PEM format.
    *   Verify `EC2_HOST` and `EC2_USERNAME` secrets.
    *   Check EC2 security group règles (Règles) to allow SSH from GitHub Actions IPs.
    *   Ensure the corresponding public key is in `~/.ssh/authorized_keys` for the `EC2_USERNAME` on the EC2 instance.
*   **Docker Pull Errors on EC2 (`manifest unknown`):**
    *   Ensure the `image` names in your `docker-compose.yml` on EC2 exactly match the base paths defined by `env.REGISTRY` and `env.DB_IMAGE`, `env.BACKEND_IMAGE`, `env.NGINX_IMAGE` in the `cd.yml` file (e.g., `ghcr.io/your-org/your-repo/db`).
    *   The `cd.yml` uses `${{ github.actor }}` to log in to ghcr.io. Ensure this actor has permissions to read the packages if they are private.
*   **`sed` command not updating `docker-compose.yml`:**
    *   Verify that the `image:` lines in your `docker-compose.yml` on EC2 match the pattern used in the `sed` commands in `cd.yml`.
*   **Permission Denied on EC2:**
    *   Ensure the `EC2_USERNAME` has necessary permissions to run Docker commands (is in `docker` group) and write to the project directory and `docker-compose.yml` if needed.
*   **Docker Compose V1 vs V2:**
    *   The script currently uses `docker-compose` (V1). If you have installed Docker Compose V2 (`docker compose`), you will need to update the commands in `cd.yml` (e.g., `docker compose down`, `docker compose up -d`).
*   **`libcrypt.so.1: cannot open shared object file` (for `docker-compose` V1 on Amazon Linux):**
    *   Install the compatibility library: `sudo yum install libxcrypt-compat -y`.

This README should provide a good starting point for understanding and using the CD workflow.
