# Weekly Status - Week 14

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- SPRINT_GOAL: Develop and promote the Inventory API with PostgreSQL, product catalog operations, security, and automated tests.
<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| Pending confirmation | Implement and promote the Inventory API | doing | https://github.com/code-corhuila/bysellens-inventory-api/pull/16 |

## 2. My individual contribution

- Prepared the Inventory API foundation with Spring Boot and PostgreSQL configuration.
- Added the product persistence model and database migration.
- Added product service operations and an outbox for product events.
- Added protected API operations and JWT security configuration.
- Added a Dockerfile, Docker Compose configuration, and execution instructions.
- Added unit, security, and PostgreSQL integration tests.
- Organized the implementation into pull requests for develop and qa.
- Prepared one pull request to promote the Inventory changes to main.

## 3. Blockers and risks

- The pull request to main was reported as open and awaiting review.
- Two integration tests were skipped because Docker was not available.
- PostgreSQL persistence was not verified against a real database.
- RabbitMQ publishing was not verified against a real broker; the publisher is disabled by default.
- The user story ID still needs to be confirmed.

## 4. Plan for next week

- Address review feedback on the Inventory pull request to main.
- Run the skipped PostgreSQL integration tests with Docker available.
- Coordinate the RabbitMQ exchange configuration and verify event publishing.
- Confirm the user story ID and update this file.
- Coordinate the Inventory frontend integration with the API.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links

- Inventory API repository: https://github.com/code-corhuila/bysellens-inventory-api
- Pull request to main: https://github.com/code-corhuila/bysellens-inventory-api/pull/16
- PostgreSQL foundation in develop: https://github.com/code-corhuila/bysellens-inventory-api/commit/f3c9921e314a0c46d571f8fddbe660ec1a38f0c4
- Product persistence model in develop: https://github.com/code-corhuila/bysellens-inventory-api/commit/d89b3919ed5b09b22b9f0865736dbdd2d6af4d83
- Product outbox in the feature branch: https://github.com/code-corhuila/bysellens-inventory-api/commit/be67f8c2a5d9416174c2375a1e528d9511cd9238
- PostgreSQL foundation in qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/a4d3077f9e62d5a971a9002914d5604948f7d67c
- Product model in qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/9d679ec7cd41df9e40687821b6f0a5be2ea11877
- Product outbox in develop: https://github.com/code-corhuila/bysellens-inventory-api/commit/195886b751c618087828632f33eed529927f12c7
- Merge of PR #9 from qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/6632b4d517a9c769ad1bde597e568936dbd35e1d
- Secured API in develop: https://github.com/code-corhuila/bysellens-inventory-api/commit/68942e5f1ffbcbe197c25bfccc84040ecc830531
- Final Inventory stage in develop: https://github.com/code-corhuila/bysellens-inventory-api/commit/9d2889cee517f2c5f644244f8ba5f8ff4068eb01
- Product outbox in qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/ded232fdbde5e43d3cc6e71f8dc188ff7a7af153
- Secured API in qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/dd39e3fb26b13bfbd5af6d2486843729e6b1824a
- Final Inventory stage in qa: https://github.com/code-corhuila/bysellens-inventory-api/commit/fbc6d80c90954cfb4891cc7f7a416d70b80551ad
