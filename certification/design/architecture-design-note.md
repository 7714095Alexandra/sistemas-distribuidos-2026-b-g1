# Architecture design note — BySellens

## 1. Target architecture

![Target architecture](target-architecture.png)

The proposed architecture places the BySellens system on Huawei Cloud. The system includes the customer, sales, product, and inventory services. Each service has its own responsibilities and communicates with other services when needed.

I propose running the inventory service in two availability zones. A load balancer will distribute requests to healthy instances. I also propose protecting inventory data with regular backups and restoration tests.

| Component in the diagram | Defined in (file of the team's -docs) |
|---|---|
| Customer service | Verify the corresponding file in `05-architecture` |
| Sales service | Verify the corresponding file in `05-architecture` |
| Product service | Verify the corresponding file in `05-architecture` |
| Inventory service | Verify the corresponding file in `05-architecture` |
| Customer database | Verify the corresponding file in `06-data` |
| Sales database | Verify the corresponding file in `06-data` |
| Product database | Verify the corresponding file in `06-data` |
| Inventory database | Verify the corresponding file in `06-data` |
| Message broker | Verify the corresponding file in `05-architecture` |

## 2. Five principles

The following decisions are proposed for a future Huawei Cloud deployment. They require validation against the team's documentation and tests before production.

| Principle | Current weakness | Evidence in -docs | Design decision | Huawei Cloud service |
|---|---|---|---|---|
| Reliability | A failure affecting the only available inventory instance could interrupt stock operations. | Verify the inventory architecture file | Run inventory instances in two availability zones and use health checks. | Elastic Load Balance (ELB) |
| Security | Access to services and databases must be controlled to reduce unauthorized access. | Verify the security documentation | Limit access through roles, permissions, and network rules. | Identity and Access Management (IAM) |
| Performance efficiency | The services need a way to handle changes in demand without causing long response times. | Verify the performance documentation | Monitor CPU utilization and adjust the number of instances. | Auto Scaling (AS) |
| Cost | Running resources continuously without checking their use can increase infrastructure costs. | Verify the deployment documentation | Monitor resource usage and select capacity according to demand. | Cloud Eye |
| Operations | Failures may be difficult to identify without monitoring and alerts. | Verify the operations documentation | Monitor service health and configure alerts for failures. | Cloud Eye |

## 3. Scalability

I propose monitoring the CPU utilization of the customer and inventory services. These values are initial design targets and must be validated with load tests.

| Service | Scaling metric | Threshold (number and unit) | Min instances | Max instances |
|---|---|---|---|---|
| Customer service | Average CPU utilization | Above 70% for 5 minutes | 2 | 4 |
| Inventory service | Average CPU utilization | Above 70% for 5 minutes | 2 | 4 |

For the inventory service, the minimum two instances should be distributed across two availability zones. When the threshold is exceeded, a new instance can be added until the maximum is reached. The team should also define a scale-in policy and test that stock updates remain consistent when several instances are running.

## 4. Disaster recovery

- **Critical data store:** Inventory database.
- **Proposed RTO:** 60 minutes.
- **Proposed RPO:** 60 minutes.
- **Proposed DR pattern:** Backup and restore.

I propose creating inventory database backups at least every 60 minutes and keeping a recoverable copy in a separate location. Under normal conditions, this frequency limits the data at risk to approximately 60 minutes, provided every backup succeeds.

To support the 60-minute RTO, the team must measure restoration time through regular tests and confirm that the database, service connections, and stock data can be recovered within that limit. Until those tests succeed, these values are design targets rather than verified results.

## 5. Automated deployment

The following pipeline is a proposal. The team must verify which tools and files already exist in its repositories before implementing it.

| Stage | Proposed trigger | Proposed tool or service | File in the team's repositories |
|---|---|---|---|
| Build | A pull request or push starts the pipeline. | GitHub Actions and the project's build tool | Verify the build workflow or configuration file |
| Test | The build stage finishes successfully. | GitHub Actions and the project's test framework | Verify the test workflow or test files |
| Image | The build and tests pass. | Docker | Verify the service Dockerfile |
| Deploy to qa | A tested image is ready for QA. | GitHub Actions or the team's deployment tool | Verify the QA deployment file |
| Deploy to production | The QA release is approved for production. | GitHub Actions or the team's deployment tool | Verify the production deployment file |
| Rollback | A production deployment fails or a critical issue is detected. | The team's deployment tool | Verify the rollback procedure or deployment file |

## 6. Decisions

- [ADR-C3-01](ADR-C3-01.md): Deploy the inventory service across two availability zones.
- [ADR-C3-02](ADR-C3-02.md): Protect inventory data with backups.
