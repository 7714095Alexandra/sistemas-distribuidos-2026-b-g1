<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo
- FULL_NAME: Monica Alexandra Diaz Tovar
- GITHUB_USER: 7714095Alexandra
- TEAM: by_sellens
- SPRINT_GOAL: Analyze the security of product registration in inventory-service.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| Not assigned | CP0–CP9 threat-modeling workshop contributions | done | [CP0](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/02260f94965b016deea37ad78fda700b7a2e968c), [CP1](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/4752d51), [CP8](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f182514) |

## 2. My individual contribution
- Reserved issue [#54](https://github.com/code-corhuila/bysellens-docs/issues/54) for the React admin UI → `inventory-service` boundary at `POST /api/v1/inventory/products`.
- CP1 documented the attack surface and trust boundaries in [01-attack-surface.md](../workshop/01-attack-surface.md), published in [commit 4752d51](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/4752d51).
- CP2 documented all six STRIDE categories, nine scenarios, risk ranking and residual-risk proposals. The initial [PR #63](https://github.com/code-corhuila/bysellens-docs/pull/63) is merged; continuation [PR #66](https://github.com/code-corhuila/bysellens-docs/pull/66) remains open and was not merged. Evidence: [S commit](https://github.com/code-corhuila/bysellens-docs/commit/d806a606c1faca02730f7d46f6f9bcf6e8c84c34), [I/D/E commit](https://github.com/code-corhuila/bysellens-docs/commit/1ce81d4b72cd5ee3f881cd8482be766085f78106), [ranking correction](https://github.com/code-corhuila/bysellens-docs/commit/eb35a9bf236f3ea31f52820923cf799f9a3617c7).
- CP3 linked the three distinct security stories: [#61 ADMIN authorization](https://github.com/code-corhuila/bysellens-docs/issues/61), [#67 JWT identity](https://github.com/code-corhuila/bysellens-docs/issues/67), and [#62 product validation](https://github.com/code-corhuila/bysellens-docs/issues/62). Project inclusion was not independently verified.
- CP4 published the proposed [security workflow](../workshop/04-security-ci/security.yml), [authorization probe](../workshop/04-security-ci/test-admin-authorization.sh), and [execution guide](../workshop/04-security-ci/HOW-TO-RUN.md). YAML parsing and Git Bash Bash syntax validation passed; the authorization probe has not run against BySellens.
- CP5 reviewed [PR #64](https://github.com/code-corhuila/bysellens-docs/pull/64). The [published review comment](https://github.com/code-corhuila/bysellens-docs/pull/64#issuecomment-6514414352) requests a concrete stolen-JWT response procedure: identify the responsible role, state the action available under the current no-revocation design, define when temporary risk acceptance ends, and test a previously issued token after logout and after expiry.
- CP6 addressed that review in the STRIDE branch: [response comment](https://github.com/code-corhuila/bysellens-docs/pull/64#issuecomment-6514751018) and [commit 52ff920](https://github.com/code-corhuila/bysellens-docs/commit/52ff920e077ca1b1af84ccbda11d40039ebc3951). T-02 is explicitly related to the downstream ProductCreated boundary, not a direct HTTP-boundary priority.
- CP7 reviewed the three backlog stories and recorded complementary relationships with security stories for other services. The README keeps the real [workshop folder](../workshop/), [#61](https://github.com/code-corhuila/bysellens-docs/issues/61), [#67](https://github.com/code-corhuila/bysellens-docs/issues/67), and [#62](https://github.com/code-corhuila/bysellens-docs/issues/62) links. The student confirmed Backlog placement; independent Project verification was unavailable.
- CP8 executed the isolated [authorization demonstration](../workshop/04-security-ci/demo/DemoAuthorizationTest.java) and documented it in [HOW-TO-RUN.md](../workshop/04-security-ci/HOW-TO-RUN.md). The real output was non-ADMIN `403` with no persistence and ADMIN `201` with in-memory persistence, using HMAC rather than RS256. It does not verify PostgreSQL or the real BySellens service. Evidence: [demo commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/e8de14a) and [documentation commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f182514).
- CP9 records the final verifiable status in this HU-status file; the CP9 guide was not available locally.

## 3. Blockers and risks
- Library call number and library registration date/time remain pending confirmation.
- The service implementation, deployment configuration, test credentials and isolated persistence query required for the authorization probe are unavailable; no BySellens runtime result is claimed.
- The CP8 demonstration uses HMAC and in-memory storage; it does not verify RS256, PostgreSQL or the real BySellens inventory-service. The real-service probe and `actionlint` remain pending.
- GitHub Project inclusion is not independently verified: `gh project list --owner code-corhuila` failed with `missing required scopes [read:project]`. The team-provided confirmation is therefore reported information, not an independent check.
- The library call number and library registration date/time remain pending. No evidence supports claims about workshop timing, push intervals or two-day completion.

## 4. Plan for next week
- Address the CP5 stolen-token procedure and add the requested logout/expiry behavior test when the service implementation and test environment are available.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration) - runtime authorization test remains pending
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- [Workshop README](../workshop/README.md) with the reflection document link and CP0–CP4 configuration.
- Reflection document: https://docs.google.com/document/d/1YDt45oYGj1d0XUBGX1jPpDSTwUM3DbhM56toLEdlWoI/edit?usp=sharing
- [CP4 workflow commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/7f9f1cf1cbd57652eb33b34fbfc5bd90d0d0c4f6)
- [CP4 documentation commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/e91ba00a71abcf41b895b5ffe1ee8514c968aa44)
- [CP7 review commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/706b310)
- [CP8 demo commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/e8de14a)
- [CP8 output documentation commit](https://github.com/7714095Alexandra/sistemas-distribuidos-2026-b-g1/commit/f182514)
