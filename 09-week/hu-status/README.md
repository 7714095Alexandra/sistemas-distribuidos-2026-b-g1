# Weekly Status - Week 09

- FULL_NAME: Alexandra
- GITHUB_USER: 7714095Alexandra
- TEAM: Bysellens
- SPRINT_GOAL: Improve the security and reliability of the By_Sellens project by reviewing configuration management, secrets handling, and progressive delivery practices for MVP 2.

## 1. User stories worked this week

| **HU ID** | **Title** | **Status (todo/doing/done)** | **Evidence (PR or commit URL)** |
| ---------- | --------- | ---------------------------- | ------------------------------- |
| HU-09-001 | Configuration and secrets management | done | https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/d856a54d152be7245ee63b29ae041885b4134cb7 |
| HU-09-002 | Feature flags and progressive delivery planning | done | https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/ebb4f08b6d2863ba5c7c7641d8fd32512988abbd |

## 2. My individual contribution

- I worked on reviewing and organizing the configuration strategy for the By_Sellens project.
- I reviewed the use of environment variables to keep configuration separated from the application code.
- I worked on the documentation of secure secrets management, avoiding sensitive information in the repository.
- I contributed to defining a feature-flag strategy to separate deployment from feature release.
- I reviewed the use of progressive delivery through canary releases and rollback strategies.
- I organized the corresponding documentation and evidence for the work completed during the week.

## 3. Blockers and risks

- Secrets and environment-specific configuration must be managed carefully to avoid exposing sensitive information.
- Missing or incorrect environment variables could cause service failures during deployment.
- Feature flags must have clear ownership and removal criteria to avoid accumulating unnecessary flags.
- Progressive delivery requires monitoring and a defined rollback strategy before releasing changes.

## 4. Plan for next week

- Continue preparing the By_Sellens project for MVP 2.
- Review configuration and environment variables required by the services.
- Continue improving security and configuration documentation.
- Review service persistence and database integration.
- Prepare the required documentation and evidence for the MVP 2 release.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (`hu-xxx-dev -> develop`, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links

- Configuration and secrets management:
  https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/d856a54d152be7245ee63b29ae041885b4134cb7

- Feature flags and progressive delivery:
  https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/ebb4f08b6d2863ba5c7c7641d8fd32512988abbd
