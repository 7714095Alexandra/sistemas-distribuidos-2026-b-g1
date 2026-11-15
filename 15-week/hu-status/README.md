# Weekly Status - Week 15

- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- SPRINT_GOAL: Prepare the Inventory API and its PostgreSQL database for release and document the work across project environments.

## 1. User stories worked this week

| **HU ID** | **Title** | **Status (todo/doing/done)** | **Evidence (PR or commit URL)** |
| ---------- | --------- | ---------------------------- | ------------------------------- |
| Pending confirmation | Inventory API implementation and release preparation | doing | https://github.com/code-corhuila/bysellens-inventory-api/pull/16 |
| Pending confirmation | Inventory database schema and release preparation | doing | https://github.com/code-corhuila/bysellens-inventory-db/pull/3 |

## 2. My individual contribution

- Worked on the Inventory API and its promotion across develop, QA, and main environments.
- Prepared pull requests for the product model, security configuration, outbox, and tests.
- Worked on the Inventory database repository and prepared its PostgreSQL migration.
- Reviewed the database migration against the Inventory API migration.
- Documented differences between the project ZIP and the current Inventory database contract.
- Prepared Inventory database pull requests for develop, QA, and main.
- Worked on version 3.0.0 tags and releases for the Inventory API and database.
- Recorded the following commits for this week's work:
  - `f12a1bbbeb81c1cc6ffd8059387eafecb4b858f8`
  - `92380ae4c085927af7927425239fe0608c79d98f`

## 3. Blockers and risks

- Two Inventory API integration tests were skipped because Docker was unavailable during the reported test execution.
- PostgreSQL and RabbitMQ were not verified against real running instances during that execution.
- The main branch pull requests required review and approval.
- The project ZIP and the current Inventory documentation describe different responsibilities for stock and reservations. This discrepancy requires team review.
- The repositories and direct URLs for the two commits still need confirmation.

## 4. Plan for next week

- Confirm the status of the Inventory API and Inventory database releases.
- Run the skipped integration tests when Docker is available.
- Verify the Inventory API with PostgreSQL and RabbitMQ in a suitable test environment.
- Continue reviewing and updating the team documentation.
- Add the final release and commit links to the project evidence.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links

- Inventory API promotion to main: https://github.com/code-corhuila/bysellens-inventory-api/pull/16
- Inventory API final stage to QA: https://github.com/code-corhuila/bysellens-inventory-api/pull/15
- Inventory database changes to develop: https://github.com/code-corhuila/bysellens-inventory-db/pull/1
- Inventory database promotion to QA: https://github.com/code-corhuila/bysellens-inventory-db/pull/2
- Inventory database promotion to main: https://github.com/code-corhuila/bysellens-inventory-db/pull/3
- Inventory API v3.0.0 release: URL pending.
- Inventory database v3.0.0 release: URL pending.
- Commit `f12a1bbbeb81c1cc6ffd8059387eafecb4b858f8`: repository and URL pending confirmation.
- Commit `92380ae4c085927af7927425239fe0608c79d98f`: repository and URL pending confirmation.
