# Docker Compose command
COMPOSE := docker compose

# PostgreSQL configuration
DB_SERVICE := postgres
DB_NAME := trees
DB_USER := postgres

# Development database dump location
DB_DUMP := trees.dump

.PHONY: app-start-services app-stop-services \
        start-db stop-db \
        export-db import-db reset-db \
        confirm-export confirm-import


# Start all services and wait until PostgreSQL is healthy (see healthcheck in docker-compose.yml).
# Then ask whether the shared database dump should be imported.
app-start-services:
	@echo "Starting services..."
	$(COMPOSE) up -d --wait
	@$(MAKE) --no-print-directory confirm-import
	@echo "Services started."


# Stop all services. Ask first whether the current database should be exported.
# If the export fails, the services are NOT stopped, so no changes are lost.
app-stop-services:
	@$(MAKE) --no-print-directory confirm-export
	$(COMPOSE) down


# Start only PostgreSQL and wait until it is healthy.
start-db:
	$(COMPOSE) up -d --wait $(DB_SERVICE)


# Stop only PostgreSQL. Ask first whether the database should be exported.
stop-db:
	@$(MAKE) --no-print-directory confirm-export
	$(COMPOSE) stop $(DB_SERVICE)


# Export the current database to the shared development dump.
# Writes to a temporary file first, so a failed export never overwrites the existing dump.
export-db:
	@echo "Exporting database..."
	@$(COMPOSE) exec -T $(DB_SERVICE) pg_dump -U $(DB_USER) -d $(DB_NAME) -Fc > $(DB_DUMP).tmp \
	  || { rm -f $(DB_DUMP).tmp; echo "Export failed. $(DB_DUMP) was not changed."; exit 1; }
	@mv $(DB_DUMP).tmp $(DB_DUMP)
	@echo "Database exported to $(DB_DUMP)."


# Import the shared development dump.
# WARNING: --clean removes existing database objects before restoring.
import-db:
	@if [ ! -f "$(DB_DUMP)" ]; then echo "No database dump found at $(DB_DUMP)."; exit 1; fi
	@echo "Importing database. This replaces your current data..."
	$(COMPOSE) exec -T $(DB_SERVICE) pg_restore -U $(DB_USER) -d $(DB_NAME) --clean --if-exists --no-owner < $(DB_DUMP)
	@echo "Database imported."


# Delete the local database completely and restore the shared dump.
reset-db:
	@if [ ! -f "$(DB_DUMP)" ]; then echo "No database dump found at $(DB_DUMP). Nothing to reset to."; exit 1; fi
	@printf "This DELETES your local database and restores $(DB_DUMP). Continue? [y/N] "; \
	read answer || answer=""; \
	case "$$answer" in \
	  [yY]|[yY][eE][sS]) \
	    $(COMPOSE) down -v && \
	    $(COMPOSE) up -d --wait $(DB_SERVICE) && \
	    $(MAKE) --no-print-directory import-db ;; \
	  *) echo "Cancelled." ;; \
	esac


# Ask whether to import the shared dump. Skipped if no dump exists.
confirm-import:
	@if [ ! -f "$(DB_DUMP)" ]; then \
	  echo "No database dump found at $(DB_DUMP). Skipping import."; \
	else \
	  echo ""; \
	  echo "A database dump is available at $(DB_DUMP)."; \
	  printf "Importing it will REPLACE your local database. Import it? [y/N] "; \
	  read answer || answer=""; \
	  case "$$answer" in \
	    [yY]|[yY][eE][sS]) $(MAKE) --no-print-directory import-db ;; \
	    *) echo "Skipping database import." ;; \
	  esac; \
	fi


# Ask whether to export the database. Skipped if PostgreSQL is not running.
confirm-export:
	@if [ -z "$$($(COMPOSE) ps --status running -q $(DB_SERVICE) 2>/dev/null)" ]; then \
	  echo "Database is not running. Skipping export."; \
	else \
	  echo ""; \
	  printf "Export the database to $(DB_DUMP) before stopping? [y/N] "; \
	  read answer || answer=""; \
	  case "$$answer" in \
	    [yY]|[yY][eE][sS]) $(MAKE) --no-print-directory export-db ;; \
	    *) echo "Skipping database export." ;; \
	  esac; \
	fi