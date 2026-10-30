# CP4 — Security CI proposal

## Purpose and scope
This proposal protects `POST /api/v1/inventory/products` across the React
admin UI → `inventory-service` boundary. It is not copied into
`.github/workflows` and does not activate a workflow in this fork.
`security.yml` scans secrets, audits Java/Spring dependencies, and runs
`test-admin-authorization.sh`. No `*Authorization*` test was found in this
workshop or in the available docs repository, so the workflow does not claim
that such a service test exists. The implementation repository and executable
tests remain `TBD`; no BySellens runtime result is claimed.

## Stories and acceptance criteria
- [Issue #61](https://github.com/code-corhuila/bysellens-docs/issues/61):
  E-01, ADMIN authorization. The negative test must return `403` and leave no
  Product persisted; the control ADMIN test must reach normal processing.
- [Issue #67](https://github.com/code-corhuila/bysellens-docs/issues/67):
  S-01, JWT identity validation. Missing or invalid authentication is `401`,
  not evidence of the `403` authorization check.
- [Issue #62](https://github.com/code-corhuila/bysellens-docs/issues/62):
  T-01, server-side product validation.

The threat model is [PR #66](https://github.com/code-corhuila/bysellens-docs/pull/66).

## Requirements and commands
The workflow needs:

1. Repository variable `INVENTORY_REPOSITORY` with the real Java service
   owner/name.
2. Secret `INVENTORY_REPOSITORY_TOKEN` with read access; never commit credentials.
3. Maven Wrapper, Maven, or Gradle Wrapper in that repository.
4. A deployed service, a non-ADMIN JWT, a valid ADMIN JWT, an isolated
   PostgreSQL connection, and two SQL queries stored as secrets. The reviewed
   docs name `inventory_db.products` and target attributes but explicitly say
   migrations and deployed resources are unverified, so the script assumes no
   table or column names.

The concrete probe is:

```text
bash 13-week/workshop/04-security-ci/test-admin-authorization.sh
```

It sends valid product data to the documented POST path with the non-ADMIN
token and requires `403`. It runs the configured precondition query, then the
configured post-request query, requiring counts zero and zero. It sends the
same data with the ADMIN token and requires `201`, then requires count one.
`psql` is the real persistence check; this script does not use a fixed `403`
response or assume a schema.

```text
INVENTORY_BASE_URL=https://service.example \
INVENTORY_NON_ADMIN_TOKEN='not-in-source-control' \
INVENTORY_ADMIN_TOKEN='not-in-source-control' \
DATABASE_URL='postgresql://isolated-test-connection' \
PERSISTENCE_CHECK_SQL_BEFORE="SELECT count(*) FROM your_verified_table WHERE your_verified_key = :'probe_code';" \
PERSISTENCE_CHECK_SQL_AFTER="SELECT count(*) FROM your_verified_table WHERE your_verified_key = :'probe_code';" \
bash 13-week/workshop/04-security-ci/test-admin-authorization.sh
```

The example values are placeholders and must not be committed. The workflow
uses repository variables/secrets instead. The probe requires `curl`, `psql`,
an isolated database, and a service configured with real JWT validation. Each
query must return one numeric row and use PostgreSQL's `:'probe_code'`
parameter. The unique code is generated per run and is never printed.

## Current evidence and limitations

The reviewed inventory documentation identifies Java + Spring Boot and requires
independent RS256, issuer, audience, expiration and ADMIN checks, but says
implementation, build, port, deployment and acceptance tests remain pending.
No authorization request, persistence check, dependency audit or actionlint
result for BySellens was executed in this workshop. The available documentation
does not provide the service repository, deployment URL, test JWTs, database
connection or a verified runtime port or verified persistence query, so the probe currently cannot run.

After those prerequisites are provided, keep this proposal in the workshop or
copy it to the service repository's `.github/workflows/`, review permissions
and secret access, and run it on a pull request. Confirm both database
assertions before treating issue #61 as verified.

Sources: `bysellens-docs/09-microservices/services/04-inventory-service/README.md`,
`runbook.md`, `07-api/authentication.md`, and
`07-api/contracts/openapi/inventory-service.yaml`.

## CP8 execution review

The workspace contains `bysellens-docs`, but no executable inventory-service
repository, build file, migration, deployment configuration or test credentials.
The inventory runbook says `Implementation repository: TBD`; accessible team
repositories did not expose an inventory implementation. Therefore the
authorization probe was not run and no `401`, `403`, `201` or persistence result
is claimed. No production data or credentials were used.

The available CP4 checks did run locally:

```text
node YAML parser: security.yml valid
Git Bash: bash -n test-admin-authorization.sh valid
actionlint: unavailable
```

The probe remains reproducible once an isolated service and database are
provided. It requires `INVENTORY_BASE_URL`, two test JWT secrets,
`DATABASE_URL`, and both configured persistence queries as described above.

## CP8 minimal demonstration module

The CP8 guide was not available locally, so its permission for a fallback
module could not be independently confirmed. The following is an isolated
demonstration module, not BySellens `inventory-service`. It uses Java's
standard HTTP server, an in-memory product repository, and a real HMAC-signed
token check for the module's `role` claim. It does not claim RS256 parity,
database parity, or runtime evidence for BySellens.

From `13-week/workshop/04-security-ci`:

```text
javac -d demo/out demo/DemoAuthorizationTest.java
java -cp demo/out demo.DemoAuthorizationTest
```

Observed locally:

```text
PASS: non-ADMIN=403, no persistence; ADMIN=201, one persistence.
```

The test starts an ephemeral localhost server, sends a fictional product with
a unique run identifier, verifies the signed non-ADMIN request receives `403`
and is absent from the in-memory repository, then verifies the signed ADMIN
control receives `201` and is stored. The token and product data are generated
inside the process and are not printed.

This result validates only the demonstrator's security configuration and
in-memory persistence. The real BySellens service, PostgreSQL persistence,
RS256 configuration and `test-admin-authorization.sh` remain unexecuted.
