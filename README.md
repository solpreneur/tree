# Books

Books is a Kotlin + Spring Boot application using PostgreSQL, Flyway, jOOQ, and OpenAPI.

## Requirements

Before starting development, make sure you have the following installed:

* **Java 21** — [Download Java 21](https://adoptium.net/temurin/releases/?version=21)
* **IntelliJ IDEA** — [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
* **Docker Desktop** — [Download Docker Desktop](https://www.docker.com/products/docker-desktop/)
* **Git** — [Download Git](https://git-scm.com/downloads)
* **Make** — available by default on macOS/Linux

---

## Quick start

If everything is already installed:

### 1. Clone the repository

```bash
git clone <repository-url>
cd books
```

### 2. Open the project

Open the `books` directory in IntelliJ IDEA.

IntelliJ should automatically detect the Gradle project.

Make sure the project and Gradle are using **Java 21**.

### 3. Start the development services

From the project root:

```bash
make app-start-services
```

This starts the services defined in `docker-compose.yml`, including PostgreSQL.

If a shared development database dump is available, you will be asked whether you want to import it.

### 4. Start the application

Start the Spring Boot application from IntelliJ using the **Run ▶** button.

No additional environment variable or database configuration is required for local development.

That's it.

---

## Running the application

The recommended way to run the application during development is from IntelliJ.

Alternatively, you can run:

```bash
./gradlew bootRun
```

---

## Database

PostgreSQL runs locally through Docker Compose.

### Start all services

```bash
make app-start-services
```

### Start only PostgreSQL

```bash
make start-db
```

### Stop PostgreSQL

```bash
make stop-db
```

### Stop all services

```bash
make app-stop-services
```

When stopping the services, you may be asked whether you want to export the current database.

If you have made development database changes that should be shared with the team, choose `y`.

The database dump is stored at:

```text
database/books.dump
```

---

## Database migrations

Database schema changes are managed by **Flyway**.

Flyway runs automatically when the application starts.

Migrations are located at:

```text
src/main/resources/db/migration/
```

Example:

```text
V1__initial_schema.sql
V2__add_book_description.sql
V3__add_book_cover.sql
```

### Important

Never modify a migration that has already been applied.

Instead, create a new migration.

For example:

```text
V4__add_book_description.sql
```

Flyway will apply the migration automatically when the application starts.

**Flyway migrations are the source of truth for the database schema.**

---

## Development database

A shared PostgreSQL development dump is stored at:

```text
database/books.dump
```

### Export

Export the current database:

```bash
make export-db
```

If the updated database state should be shared with other developers, commit the updated `books.dump`.

### Import

Import the shared development database:

```bash
make import-db
```

**Warning:** importing the dump can replace the existing local database state.

### Reset

Completely reset the local database:

```bash
make reset-db
```

This removes the local PostgreSQL Docker volume and restores the shared development dump.

**Warning:** this deletes the current local database.

See [`database/README.md`](database/README.md) for more information about the development database.

---

## jOOQ

jOOQ generates Kotlin classes from the PostgreSQL database schema.

Generated classes should **not** be edited manually.

jOOQ code generation runs automatically as part of the Gradle build.

To run it manually:

```bash
./gradlew jooqCodegen
```

After changing the database schema, Flyway applies the migration and jOOQ generates the corresponding database classes.

---

## Tests

Run all tests:

```bash
./gradlew test
```

Run the complete build:

```bash
./gradlew build
```

Before creating a pull request, make sure the complete build passes.

---

## Troubleshooting

### Java version is incorrect

Check your Java version:

```bash
java -version
```

The project requires **Java 21**.

Also verify that IntelliJ and Gradle are using Java 21.

### PostgreSQL is not running

Check the Docker services:

```bash
docker compose ps
```

Start PostgreSQL:

```bash
make start-db
```

Or start all services:

```bash
make app-start-services
```

### PostgreSQL connection fails

The local application connects automatically to the PostgreSQL database running in Docker.

The local database uses:

```text
Host:     localhost
Port:     5432
Database: books
Username: postgres
Password: postgres
```

No IntelliJ Data Source configuration is required for the application.

### jOOQ classes are missing

Run:

```bash
./gradlew jooqCodegen
```

Then reload the Gradle project in IntelliJ.

---

## Git

Do not commit:

* IDE-specific configuration
* Local environment files containing secrets
* PostgreSQL Docker data
* Build output
* Temporary files

The development database dump can be committed when its data is intentionally being shared with the team.
