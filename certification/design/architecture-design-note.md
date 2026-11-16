# Architecture design note — BySellens

## 1. Target architecture

![Target architecture](target-architecture.png)

The proposed architecture moves the BySellens system to Huawei Cloud. The system includes the customer, sales, product, and inventory services. Each service is responsible for its own functions and communicates with other services when needed.

The inventory service will run in two availability zones, as described in ADR-C3-01. A load balancer will send requests to healthy instances. The inventory database will have backups, as described in ADR-C3-02.

| Component in the diagram | Defined in (file of the team's -docs) |
|---|---|
| Customer service | PENDING: real file path |
| Sales service | PENDING: real file path |
| Product service | PENDING: real file path |
| Inventory service | PENDING: real file path |
| Customer database | PENDING: real file path |
| Sales database | PENDING: real file path |
| Product database | PENDING: real file path |
| Inventory database | PENDING: real file path |
| Message broker | PENDING: real file path |

## 2. Five principles

| Principle | Current weakness | Evidence in -docs | Design decision | Huawei Cloud service |
|---|---|---|---|---|
| Reliability | A failure could make the inventory service unavailable. | PENDING: real file path | Run inventory instances in two availability zones and use health checks. | Elastic Load Balance (ELB) |
| Security | PENDING: describe a weakness supported by the documentation. | PENDING: real file path | Restrict access to services and databases using network and identity controls. | Identity and Access Management (IAM) |
| Performance efficiency | PENDING: describe a weakness supported by the documentation. | PENDING: real file path | Monitor demand and add service instances when needed. | Auto Scaling (AS) |
| Cost | PENDING: describe a weakness supported by the documentation. | PENDING: real file path | Select resources according to demand and monitor their usage. | Cloud Eye |
| Operations | PENDING: describe a weakness supported by the documentation. | PENDING: real file path | Monitor service health and configure alerts for failures. | Cloud Eye |

## 3. Scalability

The customer and inventory services may receive different amounts of traffic. I propose monitoring their workload and adjusting the number of instances when demand changes.

| Service | Scaling metric | Threshold (number and unit) | Min instances | Max instances |
|---|---|---|---|---|
| Customer service | CPU utilization | PENDING: define a percentage and observation period | PENDING | PENDING |
| Inventory service | CPU utilization | PENDING: define a percentage and observation period | At least 2 | PENDING |

The inventory instances should be distributed across two availability zones. Scaling rules and limits must be tested before production.

## 4. Disaster recovery

- Critical data store: inventory database.
- RTO: PENDING minutes.
- RPO: PENDING minutes.
- DR pattern from chapter 11: PENDING.
- Why the pattern meets those numbers: PENDING.

I propose keeping backups of the inventory database in a separate location and testing the restoration process. The backup frequency must support the selected RPO, and restoration tests must show whether the selected RTO is achievable.

## 5. Automated deployment

The proposed pipeline should build and test changes before they are deployed. Production deployment and rollback should use verified versions of the application.

| Stage | Trigger | Tool or service | File in the team's repositories |
|---|---|---|---|
| Build | PENDING | PENDING | PENDING: real file path |
| Test | PENDING | PENDING | PENDING: real file path |
| Image | PENDING | PENDING | PENDING: real file path |
| Deploy to qa | PENDING | PENDING | PENDING: real file path |
| Deploy to production | PENDING | PENDING | PENDING: real file path |
| Rollback | PENDING | PENDING | PENDING: real file path |

## 6. Decisions

- [ADR-C3-01](ADR-C3-01.md): Deploy the inventory service across two availability zones.
- [ADR-C3-02](ADR-C3-02.md): Protect inventory data with backups.
