# Repository Guidelines

## Project Structure & Module Organization
- `admin/`, `project/`, `gateway/`, `aggregation/` are Maven modules (Spring Boot services). Each follows `src/main/java` and `src/main/resources`.
- `console-vue/` is the Vue 3 + Vite console UI; source lives in `console-vue/src` with static assets in `console-vue/public`.
- `format/` contains the Spotless license header template; `lombok.config` defines Lombok behavior.
- Service configuration files live in `*/src/main/resources/application*.yaml`.

## Build, Test, and Development Commands
Backend (Maven wrapper):
- `mvnw.cmd -DskipTests package` (or `./mvnw -DskipTests package`) builds all Java modules.
- `mvnw.cmd -pl admin -am package` builds a single module plus dependencies.
- `mvnw.cmd test` runs Java tests (only a small set exists today).

Frontend (Vite):
- `npm --prefix console-vue install` installs UI dependencies.
- `npm --prefix console-vue run dev` starts the local dev server.
- `npm --prefix console-vue run build` creates a production build.
- `npm --prefix console-vue run lint` / `npm --prefix console-vue run format` check and format UI code.

## Coding Style & Naming Conventions
- Java targets 17 (see root `pom.xml`). Keep packages under `com.nageoffer.shortlink.*` and follow existing Spring naming (e.g., `*Controller`, `*Service`, `*Mapper`).
- Spotless applies the license header from `format/license-header` during compile; new Java files should include the header.
- Frontend uses ESLint + Prettier; prefer `npm --prefix console-vue run format` before committing UI changes.

## Testing Guidelines
- Tests are located in `*/src/test/java`. The current test footprint is minimal, so add JUnit tests with clear class names like `SomethingServiceTest` when introducing logic.
- Run `mvnw.cmd test` for backend changes and `npm --prefix console-vue run lint` for UI checks.

## Commit & Pull Request Guidelines
- Recent commits follow `<type>: <summary>` (e.g., `fix: ...`, `feature: ...`, `optimize: ...`, `update README.md`). Match this format.
- PRs should describe what changed, why, and how it was verified. Include screenshots for `console-vue/` UI changes and note any config updates under `*/src/main/resources`.

## Security & Configuration Tips
- Keep secrets out of `application*.yaml`. Use environment variables or local overrides for credentials.
- Avoid committing generated artifacts like `*/target/` or `console-vue/node_modules/`.
