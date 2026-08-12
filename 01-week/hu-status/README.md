<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       01-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 01

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Jesus Ariel Gonzalez Bonilla
- GITHUB_USER: ariel5253
- TEAM: Group 1 - PRJ-FERRETERIA-V13
- SPRINT_GOAL: Turn the hardware-store cash-control brief into a bounded context map, an ADR for the architectural style, and a testable backlog of income/expense/summary user stories.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-FIN-001 | Register an income movement with date, category, amount and optional note | doing | https://github.com/code-corhuila/sistemas-distribuidos-2026-b-g1/commit/b4ae1cc |
| HU-FIN-002 | Register an expense movement with date, category, amount and optional note | todo | Pending - branch hu-fin-002-dev not opened yet |
| HU-FIN-003 | Query a period summary (day/week/month) with total income, total expense and net balance | doing | https://github.com/code-corhuila/sistemas-distribuidos-2026-b-g1/commit/b4ae1cc |
| HU-FIN-004 | Manage income and expense categories to classify each movement | todo | Pending - branch hu-fin-004-dev not opened yet |
| HU-FIN-005 | List the movement detail of a period to reconcile a balance that does not add up | todo | Pending - branch hu-fin-005-dev not opened yet |

## 2. My individual contribution
- Wrote the product brief (`prd.md`) for PRJ-FERRETERIA-V13: initial context, needs and problems, current process, open questions and business glossary (income, expense, net balance, daily cut, period, category).
- Fixed the declared stack and its boundaries: Angular frontend, Go backend, MySQL database. Explicitly scoped the product **out** of POS, invoicing, per-product inventory and ERP - the deliverable is aggregated financial control only.
- Derived the first backlog (HU-FIN-001 .. HU-FIN-005) from the "needs and problems" section, so every story traces back to a stated business need instead of a technical guess.
- Applied the Week-2 session material (see the summary below): drafted the **context map first** - a single `Finance` bounded context owning movements, categories and period summaries - before proposing any service boundary.
- Started **ADR-001 (architectural style)**: context = one administrator, low transaction volume, daily/weekly/monthly aggregate queries; decision = modular monolith in Go with a REST contract for Angular; alternatives rejected = microservices (no real scale or deploy need) and event-driven (no asynchronous integration in scope); consequences = simpler operation now, one extraction point later if reporting grows.
- Applied the microservice extraction rule from the session (real boundary **+** real scale/deploy need). Neither condition holds today, so the decision is documented as "well-designed modular monolith" rather than a distributed monolith with a shared database.
- Sketched the hexagonal layering for the Go side: `domain` (Movement, Category, Period, net-balance calculation) with no I/O, `application` (use cases), `infrastructure` (MySQL repository, HTTP handlers).

## 3. Blockers and risks
- **Open questions in the brief are still unanswered** and they block acceptance criteria: initial category catalogue, single vs. multiple users, whether a daily cut locks later edits, whether movements can be edited/voided, CSV/PDF export, and whether the net balance is per period only or also cumulative.
- The edit/void and daily-cut answers change the domain model directly (immutable ledger + reversal entries vs. mutable rows), so HU-FIN-001 and HU-FIN-002 cannot be closed until that is decided.
- Security level for system access is undefined; without it I cannot size the authentication story or decide if it belongs to Corte 1.
- No environment branches (`develop`, `qa`) exist in the repository yet, so the per-environment HU branch + PR flow could not be exercised this week - only `main` is present.
- Risk of scope creep towards a POS: the brief rules it out, and every new story must be checked against that boundary before it enters the backlog.

## 4. Plan for next week
- Close the open questions with the stakeholder and convert each answer into an acceptance criterion.
- Publish `ADR-001` as a file in the repository (Context / Decision / Alternatives / Consequences) following the session template.
- Create `develop` and `qa`, then open `hu-fin-001-dev` and `hu-fin-002-dev` with PRs to `develop`.
- Implement the `Movement` and `Category` domain in Go with unit tests for the net-balance calculation, keeping the domain free of I/O.
- Define the MySQL schema (movements, categories) and the REST contract consumed by Angular.
- Build the period-summary endpoint (day/week/month) plus an integration test against MySQL.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

Notes on the unchecked items:
- Only `main` exists so far, so no HU branch or PR to `develop` could be opened.
- Acceptance criteria stay draft until the open questions in section 3 are answered.
- No production code was written this week, so there is nothing to test yet.
- The hexagonal layering is designed but not yet materialised in code.

## 6. Evidence links
- Product brief: [`prd.md`](./prd.md) - PRJ-FERRETERIA-V13 (context, needs, current process, open questions, glossary).
- Repository structure commit: https://github.com/code-corhuila/sistemas-distribuidos-2026-b-g1/commit/b4ae1cc
- Course learning material (OVAs): https://code-corhuila.github.io/ova-web/2026-B/distribuidos/
- Session summary used for the architectural decision - vector source: [`resumen_sistemas_distribuidos_semana_2.svg`](./resumen_sistemas_distribuidos_semana_2.svg)

![Distributed Systems - Week 2 summary: distributed architectures, architectural decision, ADR and backlog](./resumen_sistemas_distribuidos_semana_2_preview.png)

Key principle taken from the material: **split for a reason, not for fashion** - a good architecture makes boundaries, contracts, trade-offs and the motive of the decision explicit.
