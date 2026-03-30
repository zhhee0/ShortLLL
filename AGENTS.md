# Repository Guidelines

## Project Structure & Module Organization
- `pom.xml` is the Maven parent; services are split into modules: `admin`, `project`, `gateway`, and `aggregation`.
- Backend source lives in `*/src/main/java` with configs in `*/src/main/resources` (for example `application.yaml`, `application-dev.yaml`, and `shardingsphere-config-*.yaml`).
- `project/src/main/resources/mapper` holds MyBatis XML mappers and `templates` holds server-rendered HTML.
- `admin/src/main/resources/lua` contains Lua scripts used by the admin service.
- `console-vue` is the Vue 3 + Vite frontend (`src`, `public`, and build artifacts in `dist`).
- Formatting assets are in `format/` (license header and Spotless config).

## Build, Test, and Development Commands
Backend (run from repo root):
- `./mvnw -pl aggregation -am package` — build all backend modules.
- `./mvnw -pl gateway -am spring-boot:run` — run the gateway service.
- `./mvnw -pl aggregation -am spring-boot:run` — run the aggregation service.
- `./mvnw -pl admin -am test` — run backend tests (if present).

Frontend (run from `console-vue`):
- `pnpm install` — install dependencies (preferred per `console-vue/README.md`).
- `pnpm dev` — start local dev server.
- `pnpm build` — production build.
- `pnpm lint` / `pnpm format` — lint and format frontend sources.

## Coding Style & Naming Conventions
- Java targets 17 (Spring Boot 3 / Spring Cloud). Use 4-space indentation and package names under `com.nageoffer.shortlink.*`.
- Spotless applies the license header from `format/license-header`; you can run `./mvnw spotless:apply` if formatting drifts.
- Frontend style is enforced by ESLint + Prettier (`tabWidth: 2`, single quotes, no semicolons).

## Testing Guidelines
- Backend tests follow Maven defaults in `*/src/test/java`. The repo currently includes a utility-style test in `admin/src/test/java`; add JUnit/Spring test deps to a module if you introduce real tests.
- Frontend has no unit/e2e runner configured; use `pnpm lint` before sending changes.

## Commit & Pull Request Guidelines
- Commit messages follow `type: summary` (observed types: `fix`, `feature`, `optimize`, `update`, `other`). Some commits prefix an issue/PR marker, e.g. `!23 fix: ...`.
- PRs should include: a concise summary, testing notes, and any config changes. Add screenshots for `console-vue` UI changes.

## Security & Configuration Tips
- Keep secrets out of `application*.yaml` and `shardingsphere-config-*.yaml`; prefer environment variables or local overrides.
- Frontend environment values live in `console-vue/.env.development`; avoid committing production credentials.
