# ADR-C3-02: Protect inventory data with backups

## Status

Proposed.

## Context

The BySellens inventory database stores product quantities and inventory movements. If data is lost or damaged, the system could show incorrect stock or be unable to process inventory operations.

This decision supports the disaster recovery section of the architecture design note.

## Decision

I propose creating regular backups of the inventory database and storing them in a separate location from the primary database. I will also test the restoration process to verify that the backups can be used to recover the data after a failure. The backup frequency and retention period must match the recovery objectives defined in the architecture design note.

## Alternatives considered

- **Keep only one database without backups:** This option is simpler and has a lower storage cost, but data could be permanently lost after a failure.

- **Export the database manually:** Manual exports can provide a copy of the data, but they depend on a person remembering to perform them and may be outdated.

## Consequences

- **Positive:** Backups provide a way to restore inventory data after accidental loss or corruption.

- **Negative:** Backups require additional storage, monitoring, and regular restoration tests.
