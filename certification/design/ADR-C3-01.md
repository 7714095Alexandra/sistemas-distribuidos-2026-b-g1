# ADR-C3-01: Deploy the inventory service across two availability zones

## Status

Proposed.

## Context

The BySellens inventory service manages product stock and records inventory movements. If the service is unavailable, users may not be able to check or update stock. A failure that affects a single availability zone could interrupt the service if all its instances run in that zone.

This decision supports the reliability and scalability sections of the architecture design note.

## Decision

I propose deploying at least one inventory service instance in each of two Huawei Cloud availability zones. A load balancer will distribute requests to healthy instances. Health checks will detect unavailable instances and stop sending requests to them. The inventory database also needs an appropriate availability and recovery strategy; deploying service instances in two zones alone does not protect the database.

## Alternatives considered

- **Use one availability zone:** This option is simpler and can cost less, but a failure in that zone could make the inventory service unavailable.

- **Use a standby instance activated manually:** This option can provide a backup, but recovery depends on someone detecting the failure and starting the standby instance.

## Consequences

- **Positive:** The inventory service can continue handling requests if one zone becomes unavailable, provided that a healthy instance and its dependencies remain available.

- **Negative:** Running instances in two zones increases infrastructure costs and requires monitoring, health checks, and a separate database recovery plan.
