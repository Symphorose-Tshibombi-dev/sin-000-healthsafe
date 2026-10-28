# common — Asynchronous Decoupling (MQ)

## Overview

Part of the [HealthSafe](../README.md) project. Holds the ActiveMQ broker shared by the
services below. It is not a service itself, so it has no port of its own.

Two destinations live on this broker, and they are different on purpose:

| | Topic `staffing-events-topic` | Topic `alert-level-topic` | Queue `equipment-failure-queue` |
|---|---|---|---|
| Producer | `staffing-service` | `alert-level-service` | `ward-service` |
| Consumer(s) | `ward-service` (any service may subscribe) | any interested service | `equipment-alert-service` |
| Delivery | Every subscriber gets a copy | Every subscriber gets a copy | Exactly one consumer gets each message |
| Persistent? | No (latest event wins) | No (latest level wins) | Yes, kept until processed |
| If the consumer is down | Misses the event | Misses the change, can `GET /alert-level` | Waits on the broker |
Rule of thumb: use a **topic** when many services may care and the latest value wins, and a
**queue** when someone must act on the message and losing one is not acceptable. The queue
design is explained in [equipment-alert-service/README.md](../equipment-alert-service/README.md).

## Shared config

Each service has its own copy of `co.wethinkcode.healthsafe.mq.MqConfig`, because every
service is an independent Maven project with no shared parent pom. The copies differ on
purpose, so each one only holds what that service uses:

| Service | Constants |
|---|---|
| `staffing-service` | `BROKER_URL`, `TOPIC` |
| `ward-service` | `BROKER_URL`, `TOPIC`, `QUEUE` |
| `equipment-alert-service` | `BROKER_URL`, `QUEUE` |

`BROKER_URL` is `tcp://localhost:61616`, `TOPIC` is `staffing-events-topic` and `QUEUE` is
`equipment-failure-queue`. If you rename a destination, change it in every copy.

## Project structure

```
common/
├── docker-compose.yml
└── README.md
```

This folder holds the broker config and notes only — the actual publish/subscribe
code belongs in the producer/consumer services listed above (their poms already
depend on `activemq-client`, and each already has
`src/main/java/co/wethinkcode/healthsafe/mq/MqConfig.java`).

## Build

Nothing to build here directly — this folder just brings up the broker used by the
services listed above.

## Run

```
docker compose up -d
```

- Broker URL for clients: `tcp://localhost:61616`
- Web console: http://localhost:8161 (default admin/admin)

Then start the producer/consumer services as usual (`mvn package && java -jar ...`
from their own directories at the project root).

## Test

```
docker compose ps          # the activemq container should show as running
```

Check the queue path by hand with the broker and `equipment-alert-service` running:

1. Open http://localhost:8161, go to Queues, and look for `equipment-failure-queue`.
2. `curl -X POST http://localhost:7034/equipment-alerts -H "Content-Type: application/json" -d '{"equipmentId":"VENT-12","wardId":"W-05","description":"Lost power","severity":"CRITICAL"}'`
3. The console shows the message enqueued and then dequeued, and `curl http://localhost:7034/equipment-alerts` lists it as processed.


## MqConfig

| Service | Constants |
|---|---|
| `staffing-service` | `BROKER_URL`, `TOPIC` |
| `ward-service` | `BROKER_URL`, `TOPIC`, `QUEUE` |
| `alert-level-service` | `BROKER_URL`, `LEVEL_TOPIC` (`alert-level-topic`) |
| `equipment-alert-service` | `BROKER_URL`, `QUEUE` |


## Status

| Piece | State |
|---|---|
| `equipment-alert-service` consuming `equipment-failure-queue` | Done, with tests |
| `ward-service` publishing to `equipment-failure-queue` (`POST /wards/{id}/equipment-failures`) | Done, with tests |
| `staffing-service` publishing changed schedules to `staffing-events-topic` | Done, with tests |
| `ward-service` subscribing to `staffing-events-topic` (`GET /wards/{id}/staffing`) | Done, with tests |
| `alert-level-service` publishing level changes to `alert-level-topic` | Done, with tests |

## Limitations

- `docker-compose.yml` has no volume, so `docker compose down` throws away anything still
  queued. A restart with `docker compose restart` keeps it.
- No healthcheck, so `docker compose ps` only shows "running", not "ready for connections".
- Educational prototype, not a live clinical alerting system.

## TODO

- Add `activemq-client` publish logic to `staffing-service` on its stage/state-change endpoint.
- Add `activemq-client` subscriber logic to consumer service(s) above, replacing any
  direct synchronous calls to `staffing-service`.
