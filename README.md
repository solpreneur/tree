# Tree API

A Spring Boot and Kotlin service that stores trees as parent–child edges in PostgreSQL and exposes them through a REST API.

## Stack

- Kotlin + Spring Boot + jOOQ
- PostgreSQL
- Flyway

## Requirements

Before starting development, make sure you have the following installed:

* **Java 21** — [Download Java 21](https://adoptium.net/temurin/releases/?version=21)
* **Docker Desktop** — [Download Docker Desktop](https://www.docker.com/products/docker-desktop/) (on Linux, Docker Engine with the Compose plugin is enough)
* **Git** — [Download Git](https://git-scm.com/downloads)
* **Make** — available by default on macOS/Linux. On Windows, see [Windows](#windows).
* **IntelliJ IDEA** (optional) — [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

Make sure Docker is running before you start.

---

## Quick start

### 1. Clone the repository

```bash
git clone <repository-url>
cd tree
```

### 2. Start the development services

From the project root:

```bash
make app-start-services
```

This starts the services defined in `docker-compose.yml`, including PostgreSQL.

If a shared development database dump (`trees.dump` in the project root) is available, you will be asked whether you want to import it. Answer `y` if you have no local database changes you want to keep.

### 3. Start the application

```bash
./gradlew bootRun
```

Or open the `tree` directory in IntelliJ IDEA, make sure the project and Gradle use **Java 21**, and start the application with the **Run ▶** button.

On startup, Flyway creates or updates the database schema automatically.

No additional environment variables or database configuration are required for local development.

### 4. Check that it works

Open Swagger UI or OpenAPI docs:

```text
http://localhost:8081/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

---

## Windows

The `make` commands use shell scripts, so on Windows run them from **WSL** (recommended) or **Git Bash** with `make` installed. They don't work in PowerShell or Command Prompt.

Without `make`, you can run Docker Compose directly. The database import and export prompts are only available through `make`.

| Make command | Docker Compose equivalent |
|---|---|
| `make app-start-services` | `docker compose up -d --wait` |
| `make start-db` | `docker compose up -d --wait postgres` |
| `make stop-db` | `docker compose stop postgres` |
| `make app-stop-services` | `docker compose down` |

In PowerShell, use `.\gradlew.bat` instead of `./gradlew`:

```powershell
.\gradlew.bat bootRun
```

---

## Database

PostgreSQL runs locally through Docker Compose.

| Task | Command |
|---|---|
| Start all services | `make app-start-services` |
| Start only PostgreSQL | `make start-db` |
| Stop PostgreSQL | `make stop-db` |
| Stop all services | `make app-stop-services` |
| Export the database to `trees.dump` | `make export-db` |
| Import `trees.dump` (replaces local data) | `make import-db` |
| Delete the local database and restore `trees.dump` | `make reset-db` |

When stopping the services, you are asked whether you want to export the current database. If you have made development database changes that should be kept, answer `y`. If the export fails, the services keep running, so nothing is lost.

The database dump is stored at:

```text
trees.dump
```

---

## Database migrations

Database schema changes are managed by **Flyway**, which runs automatically when the application starts.

Migrations are located at:

```text
src/main/resources/db/migration/
```

Example:

```text
V1__create_edge_table.sql
```

jOOQ classes are generated from these migration files during the build, so no running database is needed to compile the project.

### Important

Never modify a migration that has already been applied. Create a new migration instead, for example:

```text
V2__add_trees_table.sql
```

**Flyway migrations are the source of truth for the database schema.**

---

## Tests

Make sure Docker is running, then run:

```bash
./gradlew test
```

---

## Troubleshooting

### Java version is incorrect

Check your Java version:

```bash
java -version
```

The project requires **Java 21**. Also verify that IntelliJ and Gradle are using Java 21.

### `./gradlew: Permission denied`

On macOS or Linux, make the Gradle wrapper executable:

```bash
chmod +x gradlew
```

### PostgreSQL is not running

Check the Docker services:

```bash
docker compose ps
```

Start PostgreSQL:

```bash
make start-db
```

### PostgreSQL connection fails

The application connects to the PostgreSQL database running in Docker:

```text
Host:     localhost
Port:     5433
Database: trees
Username: postgres
Password: postgres
```

If port `5433` is already in use on your machine, stop the other service, or change the port in `docker-compose.yml` and set `DATABASE_URL`:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5434/trees ./gradlew bootRun
```

### jOOQ classes are missing in IntelliJ

Run:

```bash
./gradlew jooqCodegen
```

Then reload the Gradle project in IntelliJ.