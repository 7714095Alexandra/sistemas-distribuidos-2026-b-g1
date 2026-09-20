# Weekly Status - Week 07

- **FULL_NAME:** Alexandra
- **GITHUB_USER:** 7714095Alexandra
- **TEAM:** BY Sellens
- **SPRINT_GOAL:** Define and document inter-service communication, versioned contracts, and integration strategies for the MVP, ensuring that services can communicate safely and evolve without breaking other components.

## 1. User stories worked this week

| **HU ID** | **Title** | **Status (todo/doing/done)** | **Evidence (PR or commit URL)** |
|---|---|---|---|
| HU-01 | Inter-service communication definition | done | [Commit ea77070](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/ea770702af934674f3490369056000596de705d4) |
| HU-02 | Versioned contracts and API documentation | done | [Commit 0ba968d](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/0ba968dfe068bd15a39687e9bed7ec9764e169fd) |
| HU-03 | Integration and contract testing planning | done | [Commit 5801327](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/58013270172eed1822911330882fb9d9a302bc3a) |

## 2. My individual contribution

- I worked on defining the communication between the different services of the system.
- I reviewed synchronous and asynchronous communication alternatives using REST, gRPC, and messaging.
- I contributed to the organization and documentation of versioned contracts for service communication.
- I worked on defining the structure and rules required to maintain backward compatibility between services.
- I contributed to the planning of integration and contract testing for the MVP.
- I organized the corresponding evidence and documentation for the work completed during the week.

## 3. Blockers and risks

- Communication contracts between services must remain consistent during the development of the MVP.
- Breaking changes in an API or event contract could affect other services that depend on it.
- Service integration may require additional adjustments when implementing communication between different components.
- Contract and integration tests need to be maintained as the services evolve.

## 4. Plan for next week

- Continue implementing the communication between the project's microservices.
- Review and improve the API and event contracts.
- Continue integrating the different services of the MVP.
- Add or improve contract and integration tests.
- Verify backward compatibility when modifying existing contracts.
- Continue documenting technical evidence for the project.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (`hu-xxx-dev -> develop`, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links

- **Inter-service communication:** [Commit ea77070](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/ea770702af934674f3490369056000596de705d4)
- **Versioned contracts:** [Commit 0ba968d](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/0ba968dfe068bd15a39687e9bed7ec9764e169fd)
- **Integration and contract testing:** [Commit 5801327](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/58013270172eed1822911330882fb9d9a302bc3a)
