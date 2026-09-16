# Docker Compose command
COMPOSE := docker compose

# PostgreSQL configuration
DB_SERVICE := postgres
DB_NAME := books
DB_USER := postgres

# Development database dump location
DB_DUMP := books.dump


# Available Make commands
.PHONY: app-start-services app-stop-services \
        start-db stop-db wait-for-db \
        export-db import-db reset-db \
        confirm-export confirm-import


# Start all services defined in docker-compose.yml.
# After PostgreSQL is ready, ask whether the shared database dump
# should be imported.
app-start-services:
	@echo "Starting services..."
	$(COMPOSE) up -d
	@$(MAKE) wait-for-db
	@$(MAKE) confirm-import
	@echo "Services started."


# Stop all services.
# Before stopping, ask whether the current database should be exported
# so developers don't accidentally lose database changes.
app-stop-services:
	@$(MAKE) confirm-export
	$(COMPOSE) down


# Start only the PostgreSQL service.
start-db:
	$(COMPOSE) up -d $(DB_SERVICE)
	@$(MAKE) wait-for-db


# Stop only the PostgreSQL service.
# Ask whether the database should be exported first.
stop-db:
	@$(MAKE) confirm-export
	$(COMPOSE) stop $(DB_SERVICE)


# Wait until PostgreSQL accepts connections.
# This prevents database commands from running before PostgreSQL is ready.
wait-for-db:
	@echo "Waiting for PostgreSQL..."
	@until $(COMPOSE) exec -T $(DB_SERVICE) \
		pg_isready -U $(DB_USER) -d $(DB_NAME) > /dev/null 2>&1; do \
		sleep 1; \
	done
	@echo "PostgreSQL is ready."


# Export the current PostgreSQL database to a shared development dump.
#
# The dump can be committed to Git when the database state should be
# shared with other developers.
export-db:
	@echo "Exporting PostgreSQL database..."
	$(COMPOSE) exec -T $(DB_SERVICE) \
		pg_dump \
		-U $(DB_USER) \
		-d $(DB_NAME) \
		-Fc > $(DB_DUMP)
	@echo "Database exported to $(DB_DUMP)"


# Import the shared development database dump.
#
# WARNING: --clean removes existing database objects before restoring.
# This will replace the current database state with the dump.
import-db:
	@echo "WARNING: This will replace your existing database."
	@echo "Importing PostgreSQL database..."
	cat $(DB_DUMP) | $(COMPOSE) exec -T $(DB_SERVICE) \
		pg_restore \
		-U $(DB_USER) \
		-d $(DB_NAME) \
		--clean \
		--if-exists
	@echo "Database imported."


# Completely reset the local PostgreSQL database.
#
# The Docker volume is deleted, PostgreSQL is recreated, and the
# shared development dump is restored.
reset-db:
	@echo "Resetting development database..."
	$(COMPOSE) down -v
	$(COMPOSE) up -d $(DB_SERVICE)
	@$(MAKE) wait-for-db
	@$(MAKE) import-db
	@echo "Development database reset."


# Ask whether the developer wants to import the shared database dump.
#
# Importing the dump will replace the existing database contents.
# If no dump exists, the import is skipped automatically.
confirm-import:
	@if [ ! -f "$(DB_DUMP)" ]; then \
		echo "No database dump found at $(DB_DUMP)."; \
		echo "Skipping database import."; \
	else \
		echo ""; \
		echo "A database dump is available at $(DB_DUMP)."; \
		echo "WARNING: Importing it will RESET your existing database."; \
		printf "Do you want to import it? [y/N] " && \
		read answer && \
		case "$$answer" in \
			[yY]|[yY][eE][sS]) $(MAKE) import-db ;; \
			*) echo "Skipping database import." ;; \
		esac \
	fi


# Ask whether the developer wants to export the current database
# before stopping the services.
confirm-export:
	@echo ""; \
	printf "Export database before stopping? [y/N] " && \
	read answer && \
	case "$$answer" in \
		[yY]|[yY][eE][sS]) $(MAKE) export-db ;; \
		*) echo "Skipping database export." ;; \
	esac